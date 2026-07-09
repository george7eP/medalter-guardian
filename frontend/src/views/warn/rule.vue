<template>
  <div class="rule-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="關聯設備">
          <el-select v-model="searchForm.deviceId" placeholder="請選擇設備" clearable filterable style="width: 200px">
            <el-option label="【全局規則】(適用所有設備)" :value="null" />
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="預警級別">
          <el-select v-model="searchForm.warnLevel" placeholder="選擇級別" clearable style="width: 150px">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="緊急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜尋</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增規則</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="生效範圍" min-width="150">
          <template #default="{ row }">
            <el-tag v-if="!row.deviceId" type="info">全局規則</el-tag>
            <span v-else>{{ getDeviceName(row.deviceId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warnCondition" label="觸發條件" min-width="180" />
        <el-table-column prop="warnLevel" label="預警級別" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.warnLevel)">
              {{ levelText(row.warnLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyType" label="通知方式" min-width="150" />
        <el-table-column prop="ruleStatus" label="狀態" width="100" align="center">
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
            <el-button link type="primary" size="small" @click="handleEdit(row)">編輯</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">刪除</el-button>
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
        <el-form-item label="生效範圍" required>
          <el-select v-model="ruleForm.deviceId" placeholder="選擇設備 (不選則為全局規則)" clearable filterable style="width: 100%">
            <el-option label="【全局規則】(適用所有設備)" :value="null" />
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="觸發條件" required>
          <el-input v-model="ruleForm.warnCondition" placeholder="例如: days <= warn_days (距檢修不足警告天數)" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="預警級別" required>
              <el-select v-model="ruleForm.warnLevel" style="width: 100%">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="緊急" value="URGENT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="規則狀態">
              <el-switch v-model="ruleForm.ruleStatus" :active-value="1" :inactive-value="0" active-text="啟用" inactive-text="停用" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="通知方式" required>
          <el-select v-model="notifyTypeArr" multiple placeholder="可多選" style="width: 100%">
            <el-option label="系統通知 (SYSTEM)" value="SYSTEM" />
            <el-option label="電子郵件 (EMAIL)" value="EMAIL" />
            <el-option label="簡訊通知 (SMS)" value="SMS" />
          </el-select>
        </el-form-item>

        <el-form-item label="備註">
          <el-input v-model="ruleForm.remark" type="textarea" :rows="2" placeholder="選填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">確定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
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

// 為了方便多選，特別用一個數組綁定 notifyType
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
    console.error('獲取設備選單失敗', error)
  }
}

const getDeviceName = (id: number) => {
  const device = deviceOptions.value.find(d => d.id === id)
  return device ? device.deviceName : `未知設備(${id})`
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
  if (level === 'URGENT') return '緊急'
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
    console.error('獲取規則列表失敗:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchRuleList() }
const resetSearch = () => { searchForm.deviceId = undefined; searchForm.warnLevel = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增預警規則'
  notifyTypeArr.value = ['SYSTEM'] // 預設勾選系統通知
  Object.assign(ruleForm, {
    id: undefined, deviceId: null, warnCondition: '', warnLevel: 'MEDIUM',
    notifyType: '', ruleStatus: 1, remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: WarnRule) => {
  isEdit.value = true
  dialogTitle.value = '編輯預警規則'
  notifyTypeArr.value = row.notifyType ? row.notifyType.split(',') : []
  Object.assign(ruleForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!ruleForm.warnCondition || notifyTypeArr.value.length === 0) {
    ElMessage.warning('請填寫觸發條件並選擇至少一種通知方式')
    return
  }
  submitLoading.value = true
  // 將陣列轉回字串格式存入資料庫 (例: "SYSTEM,EMAIL")
  ruleForm.notifyType = notifyTypeArr.value.join(',')
  
  try {
    if (isEdit.value && ruleForm.id) {
      await updateRule(ruleForm.id, ruleForm)
      ElMessage.success('規則更新成功')
    } else {
      await createRule(ruleForm)
      ElMessage.success('規則添加成功')
    }
    dialogVisible.value = false
    fetchRuleList()
  } catch (error) {
    console.error('保存規則失敗:', error)
  } finally {
    submitLoading.value = false
  }
}

// 快速切換狀態
const handleStatusChange = async (row: WarnRule, newStatus: number) => {
  try {
    await updateRule(row.id!, { ...row, ruleStatus: newStatus })
    ElMessage.success(`規則已${newStatus === 1 ? '啟用' : '停用'}`)
  } catch (error) {
    row.ruleStatus = newStatus === 1 ? 0 : 1 // 失敗則恢復狀態
  }
}

const handleDelete = async (row: WarnRule) => {
  try {
    await ElMessageBox.confirm('確定要刪除這條規則嗎？', '警告', { type: 'error' })
    await deleteRule(row.id!)
    ElMessage.success('刪除成功')
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