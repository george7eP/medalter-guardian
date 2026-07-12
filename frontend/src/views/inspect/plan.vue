<template>
  <div class="plan-container">
    <PageHeader title="检修计划" subtitle="设备检修计划安排与跟踪" />
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关联设备">
          <el-select v-model="searchForm.deviceId" placeholder="请选择设备" clearable filterable style="width: 200px">
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划状态">
          <el-select v-model="searchForm.planStatus" placeholder="选择状态" clearable style="width: 150px">
            <el-option label="待执行" value="PENDING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜寻</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增检修计划</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="关联设备" min-width="150">
          <template #default="{ row }">
            {{ getDeviceName(row.deviceId) }}
          </template>
        </el-table-column>
        <el-table-column prop="planDate" label="计划日期" width="120" align="center" />
        <el-table-column prop="inspectContent" label="检修内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="principal" label="负责人" width="120" align="center" />
        <el-table-column prop="planStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="enumTag(PLAN_STATUS, row.planStatus)" round>
              {{ enumLabel(PLAN_STATUS, row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="fetchPlanList"
          @current-change="fetchPlanList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="planForm" label-width="90px">
        <el-form-item label="关联设备" required>
          <el-select v-model="planForm.deviceId" placeholder="请选择要检修的设备" filterable style="width: 100%">
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期" required>
          <el-date-picker v-model="planForm.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人" required>
          <el-input v-model="planForm.principal" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="检修内容" required>
          <el-input v-model="planForm.inspectContent" type="textarea" :rows="3" placeholder="请描述预计要进行的检修项目" />
        </el-form-item>
        <el-form-item label="计划状态">
          <el-radio-group v-model="planForm.planStatus">
            <el-radio value="PENDING">待执行</el-radio>
            <el-radio value="COMPLETED">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="planForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getPlanList, createPlan, updatePlan, deletePlan } from "@/api/inspect/plan"
import type { InspectPlan } from "@/api/inspect/plan"
import { getDeviceList } from "@/api/device" // 引入设备 API 以获取下拉选单
import type { DeviceInfo } from "@/api/device"
import { PLAN_STATUS, enumTag, enumLabel } from "@/constants/enums"

const tableData = ref<InspectPlan[]>([])
const deviceOptions = ref<DeviceInfo[]>([]) // 存放设备清单
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const searchForm = reactive({ deviceId: undefined as number | undefined, planStatus: '' })
const pageParams = reactive({ page: 1, pageSize: 10 })

const planForm = reactive<InspectPlan>({
  id: undefined,
  deviceId: undefined,
  planDate: '',
  inspectContent: '',
  principal: '',
  planStatus: 'PENDING',
  remark: ''
})

// 获取设备清单 (用于下拉选单与名称对应)
const fetchDeviceOptions = async () => {
  try {
    const res: any = await getDeviceList({ page: 1, pageSize: 1000 })
    deviceOptions.value = res.records || res.list || []
  } catch (error) {
    console.error('获取设备选单失败', error)
  }
}

// 根据 ID 匹配设备名称
const getDeviceName = (id: number) => {
  const device = deviceOptions.value.find(d => d.id === id)
  return device ? device.deviceName : `未知设备(${id})`
}

const fetchPlanList = async () => {
  loading.value = true
  try {
    const params = {
      page: pageParams.page,
      pageSize: pageParams.pageSize,
      deviceId: searchForm.deviceId,
      planStatus: searchForm.planStatus || undefined
    }
    const result: any = await getPlanList(params)
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取计划列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchPlanList() }
const resetSearch = () => { searchForm.deviceId = undefined; searchForm.planStatus = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增检修计划'
  Object.assign(planForm, {
    id: undefined, deviceId: undefined, planDate: '', inspectContent: '',
    principal: '', planStatus: 'PENDING', remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: InspectPlan) => {
  isEdit.value = true
  dialogTitle.value = '编辑检修计划'
  Object.assign(planForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!planForm.deviceId || !planForm.planDate || !planForm.inspectContent) {
    ElMessage.warning('请填写必填项目')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && planForm.id) {
      await updatePlan(planForm.id, planForm)
      ElMessage.success('计划更新成功')
    } else {
      await createPlan(planForm)
      ElMessage.success('计划添加成功')
    }
    dialogVisible.value = false
    fetchPlanList()
  } catch (error) {
    console.error('保存计划失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: InspectPlan) => {
  try {
    await ElMessageBox.confirm('确定要删除这条检修计划吗？', '警告', { type: 'error' })
    await deletePlan(row.id!)
    ElMessage.success('删除成功')
    fetchPlanList()
  } catch {}
}

onMounted(() => {
  fetchDeviceOptions().then(() => {
    fetchPlanList()
  })
})
</script>

<style scoped>
.plan-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.toolbar { margin-bottom: 15px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>