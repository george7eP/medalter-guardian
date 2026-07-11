<template>
  <div class="record-container">
    <PageHeader title="检修记录" subtitle="历史检修执行记录归档" />
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关联设备">
          <el-select v-model="searchForm.deviceId" placeholder="请选择设备" clearable filterable style="width: 200px">
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="检修结果">
          <el-select v-model="searchForm.inspectResult" placeholder="选择结果" clearable style="width: 150px">
            <el-option label="合格" value="PASS" />
            <el-option label="部分合格" value="PARTIAL" />
            <el-option label="不合格" value="FAIL" />
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
        <el-button type="primary" @click="handleAdd">新增检修记录</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="关联设备" min-width="150">
          <template #default="{ row }">
            {{ getDeviceName(row.deviceId) }}
          </template>
        </el-table-column>
        <el-table-column prop="inspectDate" label="检修日期" width="120" align="center" />
        <el-table-column prop="inspectContent" label="检修内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="inspectResult" label="检修结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="resultTagType(row.inspectResult)">
              {{ resultText(row.inspectResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="110" align="center" />
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
          @size-change="fetchRecordList"
          @current-change="fetchRecordList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form :model="recordForm" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联设备" required>
              <el-select v-model="recordForm.deviceId" placeholder="选择设备" filterable style="width: 100%">
                <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检修日期" required>
              <el-date-picker v-model="recordForm.inspectDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="检修内容" required>
          <el-input v-model="recordForm.inspectContent" type="textarea" :rows="2" placeholder="请描述实际进行的检修项目" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检修结果" required>
              <el-select v-model="recordForm.inspectResult" style="width: 100%">
                <el-option label="合格" value="PASS" />
                <el-option label="部分合格" value="PARTIAL" />
                <el-option label="不合格" value="FAIL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作人" required>
              <el-input v-model="recordForm.operator" placeholder="操作人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="报告文件">
          <el-input v-model="recordForm.reportFile" placeholder="请输入报告存档路径或名称 (开发中: 暂代档案上传)" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" placeholder="选填" />
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
import { getRecordList, createRecord, updateRecord, deleteRecord } from "@/api/inspect/record"
import type { InspectRecord } from "@/api/inspect/record"
import { getDeviceList } from "@/api/device" 
import type { DeviceInfo } from "@/api/device"

const tableData = ref<InspectRecord[]>([])
const deviceOptions = ref<DeviceInfo[]>([]) 
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const searchForm = reactive({ deviceId: undefined as number | undefined, inspectResult: '' })
const pageParams = reactive({ page: 1, pageSize: 10 })

const recordForm = reactive<InspectRecord>({
  id: undefined,
  deviceId: undefined,
  inspectDate: '',
  inspectContent: '',
  inspectResult: 'PASS',
  reportFile: '',
  operator: '',
  remark: ''
})

// 获取设备清单
const fetchDeviceOptions = async () => {
  try {
    const res: any = await getDeviceList({ page: 1, pageSize: 1000 })
    deviceOptions.value = res.records || res.list || []
  } catch (error) {
    console.error('获取设备选单失败', error)
  }
}

const getDeviceName = (id: number) => {
  const device = deviceOptions.value.find(d => d.id === id)
  return device ? device.deviceName : `未知设备(${id})`
}

const resultTagType = (result: string) => {
  if (result === 'PASS') return 'success'
  if (result === 'PARTIAL') return 'warning'
  return 'danger'
}

const resultText = (result: string) => {
  if (result === 'PASS') return '合格'
  if (result === 'PARTIAL') return '部分合格'
  return '不合格'
}

const fetchRecordList = async () => {
  loading.value = true
  try {
    const params = {
      page: pageParams.page,
      pageSize: pageParams.pageSize,
      deviceId: searchForm.deviceId,
      inspectResult: searchForm.inspectResult || undefined
    }
    const result: any = await getRecordList(params)
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取记录列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchRecordList() }
const resetSearch = () => { searchForm.deviceId = undefined; searchForm.inspectResult = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增检修记录'
  Object.assign(recordForm, {
    id: undefined, deviceId: undefined, inspectDate: '', inspectContent: '',
    inspectResult: 'PASS', reportFile: '', operator: '', remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: InspectRecord) => {
  isEdit.value = true
  dialogTitle.value = '编辑检修记录'
  Object.assign(recordForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!recordForm.deviceId || !recordForm.inspectDate || !recordForm.inspectContent || !recordForm.operator) {
    ElMessage.warning('请填写必填项目')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && recordForm.id) {
      await updateRecord(recordForm.id, recordForm)
      ElMessage.success('记录更新成功')
    } else {
      await createRecord(recordForm)
      ElMessage.success('记录添加成功')
    }
    dialogVisible.value = false
    fetchRecordList()
  } catch (error) {
    console.error('保存记录失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: InspectRecord) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '警告', { type: 'error' })
    await deleteRecord(row.id!)
    ElMessage.success('删除成功')
    fetchRecordList()
  } catch {}
}

onMounted(() => {
  fetchDeviceOptions().then(() => {
    fetchRecordList()
  })
})
</script>

<style scoped>
.record-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.toolbar { margin-bottom: 15px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>