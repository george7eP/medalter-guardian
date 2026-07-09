<template>
  <div class="record-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="關聯設備">
          <el-select v-model="searchForm.deviceId" placeholder="請選擇設備" clearable filterable style="width: 200px">
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="檢修結果">
          <el-select v-model="searchForm.inspectResult" placeholder="選擇結果" clearable style="width: 150px">
            <el-option label="合格" value="PASS" />
            <el-option label="部分合格" value="PARTIAL" />
            <el-option label="不合格" value="FAIL" />
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
        <el-button type="primary" @click="handleAdd">新增檢修記錄</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="關聯設備" min-width="150">
          <template #default="{ row }">
            {{ getDeviceName(row.deviceId) }}
          </template>
        </el-table-column>
        <el-table-column prop="inspectDate" label="檢修日期" width="120" align="center" />
        <el-table-column prop="inspectContent" label="檢修內容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="inspectResult" label="檢修結果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="resultTagType(row.inspectResult)">
              {{ resultText(row.inspectResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="110" align="center" />
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
          @size-change="fetchRecordList"
          @current-change="fetchRecordList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form :model="recordForm" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="關聯設備" required>
              <el-select v-model="recordForm.deviceId" placeholder="選擇設備" filterable style="width: 100%">
                <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="檢修日期" required>
              <el-date-picker v-model="recordForm.inspectDate" type="date" value-format="YYYY-MM-DD" placeholder="選擇日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="檢修內容" required>
          <el-input v-model="recordForm.inspectContent" type="textarea" :rows="2" placeholder="請描述實際進行的檢修項目" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="檢修結果" required>
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

        <el-form-item label="報告文件">
          <el-input v-model="recordForm.reportFile" placeholder="請輸入報告存檔路徑或名稱 (開發中: 暫代檔案上傳)" />
        </el-form-item>

        <el-form-item label="備註">
          <el-input v-model="recordForm.remark" placeholder="選填" />
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

// 獲取設備清單
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
    console.error('獲取記錄列表失敗:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchRecordList() }
const resetSearch = () => { searchForm.deviceId = undefined; searchForm.inspectResult = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增檢修記錄'
  Object.assign(recordForm, {
    id: undefined, deviceId: undefined, inspectDate: '', inspectContent: '',
    inspectResult: 'PASS', reportFile: '', operator: '', remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: InspectRecord) => {
  isEdit.value = true
  dialogTitle.value = '編輯檢修記錄'
  Object.assign(recordForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!recordForm.deviceId || !recordForm.inspectDate || !recordForm.inspectContent || !recordForm.operator) {
    ElMessage.warning('請填寫必填項目')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && recordForm.id) {
      await updateRecord(recordForm.id, recordForm)
      ElMessage.success('記錄更新成功')
    } else {
      await createRecord(recordForm)
      ElMessage.success('記錄添加成功')
    }
    dialogVisible.value = false
    fetchRecordList()
  } catch (error) {
    console.error('保存記錄失敗:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: InspectRecord) => {
  try {
    await ElMessageBox.confirm('確定要刪除這條記錄嗎？', '警告', { type: 'error' })
    await deleteRecord(row.id!)
    ElMessage.success('刪除成功')
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