<template>
  <div class="rule-container">
    <PageHeader title="预警规则" subtitle="设备到期与状态预警规则配置" />
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关联设备">
          <el-select v-model="searchForm.deviceId" placeholder="请选择设备" clearable filterable style="width: 200px">
            <el-option label="【全局规则】(适用所有设备)" :value="null" />
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select v-model="searchForm.warnLevel" placeholder="选择级别" clearable style="width: 150px">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
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
        <el-button type="primary" @click="handleAdd">新增规则</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="生效范围" min-width="150">
          <template #default="{ row }">
            <el-tag v-if="!row.deviceId" type="info">全局规则</el-tag>
            <span v-else>{{ getDeviceName(row.deviceId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warnCondition" label="触发条件" min-width="180" />
        <el-table-column prop="warnLevel" label="预警级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.warnLevel)">
              {{ levelText(row.warnLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyType" label="通知方式" min-width="150" />
        <el-table-column prop="ruleStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.ruleStatus"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number | string | boolean) => handleStatusChange(row, val as number)"
            />
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
          @size-change="fetchRuleList"
          @current-change="fetchRuleList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="生效范围" required>
          <el-select v-model="ruleForm.deviceId" placeholder="选择设备 (不选则为全局规则)" clearable filterable style="width: 100%">
            <el-option label="【全局规则】(适用所有设备)" :value="null" />
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="触发条件" required>
          <el-input v-model="ruleForm.warnCondition" placeholder="例如: days <= warn_days (距检修不足警告天数)" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预警级别" required>
              <el-select v-model="ruleForm.warnLevel" style="width: 100%">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="URGENT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则状态">
              <el-switch v-model="ruleForm.ruleStatus" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="通知方式" required>
          <el-select v-model="notifyTypeArr" multiple placeholder="可多选" style="width: 100%">
            <el-option label="系统通知 (SYSTEM)" value="SYSTEM" />
            <el-option label="电子邮件 (EMAIL)" value="EMAIL" />
            <el-option label="简讯通知 (SMS)" value="SMS" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="ruleForm.remark" type="textarea" :rows="2" placeholder="选填" />
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
import { getRuleList, createRule, updateRule, deleteRule } from "@/api/warn/rule"
import type { WarnRule } from "@/api/warn/rule"
import { getDeviceList } from "@/api/device" 
import type { DeviceInfo } from "@/api/device"

const tableData = ref<WarnRule[]>([])
const deviceOptions = ref<DeviceInfo[]>([]) 
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const searchForm = reactive({ deviceId: undefined as number | undefined | null, warnLevel: '' })
const pageParams = reactive({ page: 1, pageSize: 10 })

// 为了方便多选，特别用一个数组绑定 notifyType
const notifyTypeArr = ref<string[]>([])

const ruleForm = reactive<WarnRule>({
  id: undefined,
  deviceId: null,
  warnCondition: '',
  warnLevel: 'MEDIUM',
  notifyType: '',
  ruleStatus: 1,
  remark: ''
})

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

const levelTagType = (level: string) => {
  if (level === 'LOW') return 'info'
  if (level === 'MEDIUM') return 'warning'
  if (level === 'HIGH') return 'danger'
  return 'danger'
}

const levelText = (level: string) => {
  if (level === 'LOW') return '低'
  if (level === 'MEDIUM') return '中'
  if (level === 'HIGH') return '高'
  if (level === 'URGENT') return '紧急'
  return level
}

const fetchRuleList = async () => {
  loading.value = true
  try {
    const params = {
      page: pageParams.page,
      pageSize: pageParams.pageSize,
      deviceId: searchForm.deviceId,
      warnLevel: searchForm.warnLevel || undefined
    }
    const result: any = await getRuleList(params)
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取规则列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchRuleList() }
const resetSearch = () => { searchForm.deviceId = undefined; searchForm.warnLevel = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增预警规则'
  notifyTypeArr.value = ['SYSTEM'] // 预设勾选系统通知
  Object.assign(ruleForm, {
    id: undefined, deviceId: null, warnCondition: '', warnLevel: 'MEDIUM',
    notifyType: '', ruleStatus: 1, remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: WarnRule) => {
  isEdit.value = true
  dialogTitle.value = '编辑预警规则'
  notifyTypeArr.value = row.notifyType ? row.notifyType.split(',') : []
  Object.assign(ruleForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!ruleForm.warnCondition || notifyTypeArr.value.length === 0) {
    ElMessage.warning('请填写触发条件并选择至少一种通知方式')
    return
  }
  submitLoading.value = true
  // 将阵列转回字串格式存入资料库 (例: "SYSTEM,EMAIL")
  ruleForm.notifyType = notifyTypeArr.value.join(',')
  
  try {
    if (isEdit.value && ruleForm.id) {
      await updateRule(ruleForm.id, ruleForm)
      ElMessage.success('规则更新成功')
    } else {
      await createRule(ruleForm)
      ElMessage.success('规则添加成功')
    }
    dialogVisible.value = false
    fetchRuleList()
  } catch (error) {
    console.error('保存规则失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 快速切换状态
const handleStatusChange = async (row: WarnRule, newStatus: number) => {
  try {
    await updateRule(row.id!, { ...row, ruleStatus: newStatus })
    ElMessage.success(`规则已${newStatus === 1 ? '启用' : '停用'}`)
  } catch (error) {
    row.ruleStatus = newStatus === 1 ? 0 : 1 // 失败则恢复状态
  }
}

const handleDelete = async (row: WarnRule) => {
  try {
    await ElMessageBox.confirm('确定要删除这条规则吗？', '警告', { type: 'error' })
    await deleteRule(row.id!)
    ElMessage.success('删除成功')
    fetchRuleList()
  } catch {}
}

onMounted(() => {
  fetchDeviceOptions().then(() => {
    fetchRuleList()
  })
})
</script>

<style scoped>
.rule-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.toolbar { margin-bottom: 15px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>