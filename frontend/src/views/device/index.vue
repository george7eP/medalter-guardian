<template>
  <div class="device-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="設備名稱">
          <el-input v-model="searchForm.deviceName" placeholder="請輸入設備名稱" clearable />
        </el-form-item>
        <el-form-item label="設備狀態">
          <el-select v-model="searchForm.deviceStatus" placeholder="選擇狀態" clearable style="width: 150px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="預警" value="WARN" />
            <el-option label="故障" value="FAULT" />
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
        <el-button type="primary" @click="handleAdd">新增設備</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="deviceName" label="設備名稱" min-width="130" show-overflow-tooltip />
        <el-table-column prop="deviceModel" label="型號" width="110" />
        <el-table-column prop="useDepartment" label="使用科室" width="120" align="center" />
        <el-table-column prop="manufacturer" label="生產廠家" min-width="120" show-overflow-tooltip />
        <el-table-column prop="purchaseDate" label="購買日期" width="120" align="center" />
        <el-table-column prop="inspectCycle" label="檢修週期(天)" width="110" align="center" />
        <el-table-column prop="deviceStatus" label="狀態" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.deviceStatus)">
              {{ statusText(row.deviceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastInspectDate" label="上次檢修日期" width="120" align="center" />
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
          @size-change="fetchDeviceList"
          @current-change="fetchDeviceList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="deviceForm" label-width="110px" style="padding-right: 20px;">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="設備名稱" required>
              <el-input v-model="deviceForm.deviceName" placeholder="請輸入設備名稱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="設備型號">
              <el-input v-model="deviceForm.deviceModel" placeholder="例如: CT-2000" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="使用科室" required>
              <el-input v-model="deviceForm.useDepartment" placeholder="例如: 放射科" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生產廠家">
              <el-input v-model="deviceForm.manufacturer" placeholder="請輸入製造商" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="檢修週期(天)" required>
              <el-input-number v-model="deviceForm.inspectCycle" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提前預警(天)" required>
              <el-input-number v-model="deviceForm.warnDays" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="購買日期">
              <el-date-picker v-model="deviceForm.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="選擇日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="設備狀態">
              <el-select v-model="deviceForm.deviceStatus" style="width: 100%">
                <el-option label="正常" value="NORMAL" />
                <el-option label="預警" value="WARN" />
                <el-option label="故障" value="FAULT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="備註內容">
          <el-input v-model="deviceForm.remark" type="textarea" :rows="3" placeholder="請輸入備註說明" />
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
import { getDeviceList, createDevice, updateDevice, deleteDevice } from "@/api/device"
import type { DeviceInfo } from "@/api/device"

const tableData = ref<DeviceInfo[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const searchForm = reactive({ deviceName: '', deviceStatus: '' })
const pageParams = reactive({ page: 1, pageSize: 10 })

const deviceForm = reactive<DeviceInfo>({
  id: undefined,
  deviceName: '',
  deviceModel: '',
  manufacturer: '',
  purchaseDate: '',
  useDepartment: '',
  inspectCycle: 90,
  warnDays: 7,
  deviceStatus: 'NORMAL',
  remark: ''
})

const fetchDeviceList = async () => {
  loading.value = true
  try {
    const result: any = await getDeviceList({
      page: pageParams.page,
      pageSize: pageParams.pageSize,
      deviceName: searchForm.deviceName || undefined,
      deviceStatus: searchForm.deviceStatus || undefined
    })
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('獲取設備列表失敗:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchDeviceList() }
const resetSearch = () => { searchForm.deviceName = ''; searchForm.deviceStatus = ''; handleSearch() }

const statusTagType = (status: string) => {
  if (status === 'NORMAL') return 'success'
  if (status === 'WARN') return 'warning'
  return 'danger'
}

const statusText = (status: string) => {
  if (status === 'NORMAL') return '正常'
  if (status === 'WARN') return '預警'
  return '故障'
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增設備'
  Object.assign(deviceForm, {
    id: undefined, deviceName: '', deviceModel: '', manufacturer: '',
    purchaseDate: '', useDepartment: '', inspectCycle: 90, warnDays: 7,
    deviceStatus: 'NORMAL', remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: DeviceInfo) => {
  isEdit.value = true
  dialogTitle.value = '編輯設備'
  Object.assign(deviceForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!deviceForm.deviceName || !deviceForm.useDepartment) {
    ElMessage.warning('請填寫必填項目')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && deviceForm.id) {
      await updateDevice(deviceForm.id, deviceForm)
      ElMessage.success('設備更新成功')
    } else {
      await createDevice(deviceForm)
      ElMessage.success('設備添加成功')
    }
    dialogVisible.value = false
    fetchDeviceList()
  } catch (error) {
    console.error('保存設備失敗:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: DeviceInfo) => {
  try {
    await ElMessageBox.confirm(`確定要刪除設備 "${row.deviceName}" 嗎？`, '警告', { type: 'error' })
    await deleteDevice(row.id!)
    ElMessage.success('刪除成功')
    fetchDeviceList()
  } catch {}
}

onMounted(fetchDeviceList)
</script>

<style scoped>
.device-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.toolbar { margin-bottom: 15px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>