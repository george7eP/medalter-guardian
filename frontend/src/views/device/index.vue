<template>
  <div class="device-container">
    <PageHeader title="设备管理" subtitle="医疗设备台账与运行状态维护" />
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.deviceName" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="searchForm.deviceStatus" placeholder="选择状态" clearable style="width: 150px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="预警" value="WARN" />
            <el-option label="故障" value="FAULT" />
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
        <el-button type="primary" @click="handleAdd">新增设备</el-button>
        <div class="toolbar-right">
          <span class="sort-label">排序</span>
          <el-select v-model="sortField" @change="handleSearch" style="width: 190px">
            <el-option label="默认 (ID ↑)" value="id-asc" />
            <el-option label="上次检修 ↓ 近→远" value="lastInspectDate" />
            <el-option label="上次检修 ↑ 远→近" value="lastInspectDate-asc" />
          </el-select>
        </div>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="64" align="center" />
        <el-table-column prop="deviceName" label="设备名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="deviceStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="enumTag(DEVICE_STATUS, row.deviceStatus)" effect="light" round>
              {{ enumLabel(DEVICE_STATUS, row.deviceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceModel" label="型号" width="100" show-overflow-tooltip />
        <el-table-column prop="useDepartment" label="使用科室" width="100" align="center" />
        <el-table-column prop="manufacturer" label="生产厂家" min-width="110" show-overflow-tooltip />
        <el-table-column prop="inspectCycle" label="检修周期" width="90" align="center">
          <template #default="{ row }">{{ row.inspectCycle }} 天</template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="购买日期" width="112" align="center" />
        <el-table-column prop="lastInspectDate" label="上次检修" width="112" align="center" />
        <el-table-column fixed="right" label="操作" width="130" align="center">
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
          @size-change="fetchDeviceList"
          @current-change="fetchDeviceList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="deviceForm" label-width="110px" style="padding-right: 20px;">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" required>
              <el-input v-model="deviceForm.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备型号">
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
            <el-form-item label="生产厂家">
              <el-input v-model="deviceForm.manufacturer" placeholder="请输入制造商" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检修周期(天)" required>
              <el-input-number v-model="deviceForm.inspectCycle" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提前预警(天)" required>
              <el-input-number v-model="deviceForm.warnDays" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="购买日期">
              <el-date-picker v-model="deviceForm.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备状态">
              <el-select v-model="deviceForm.deviceStatus" style="width: 100%">
                <el-option label="正常" value="NORMAL" />
                <el-option label="预警" value="WARN" />
                <el-option label="故障" value="FAULT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注内容">
          <el-input v-model="deviceForm.remark" type="textarea" :rows="3" placeholder="请输入备注说明" />
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
import { getDeviceList, createDevice, updateDevice, deleteDevice } from "@/api/device"
import type { DeviceInfo } from "@/api/device"
import { DEVICE_STATUS, enumTag, enumLabel } from "@/constants/enums"

const tableData = ref<DeviceInfo[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const searchForm = reactive({ deviceName: '', deviceStatus: '' })
const pageParams = reactive({ page: 1, pageSize: 10 })
const sortField = ref('id-asc')

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
      deviceStatus: searchForm.deviceStatus || undefined,
      sortField: sortField.value.replace('-asc', ''),
      sortOrder: sortField.value.endsWith('-asc') ? 'asc' : 'desc'
    })
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchDeviceList() }
const resetSearch = () => { searchForm.deviceName = ''; searchForm.deviceStatus = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增设备'
  Object.assign(deviceForm, {
    id: undefined, deviceName: '', deviceModel: '', manufacturer: '',
    purchaseDate: '', useDepartment: '', inspectCycle: 90, warnDays: 7,
    deviceStatus: 'NORMAL', remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: DeviceInfo) => {
  isEdit.value = true
  dialogTitle.value = '编辑设备'
  Object.assign(deviceForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!deviceForm.deviceName || !deviceForm.useDepartment) {
    ElMessage.warning('请填写必填项目')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && deviceForm.id) {
      await updateDevice(deviceForm.id, deviceForm)
      ElMessage.success('设备更新成功')
    } else {
      await createDevice(deviceForm)
      ElMessage.success('设备添加成功')
    }
    dialogVisible.value = false
    fetchDeviceList()
  } catch (error) {
    console.error('保存设备失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: DeviceInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除设备 "${row.deviceName}" 吗？`, '警告', { type: 'error' })
    await deleteDevice(row.id!)
    ElMessage.success('删除成功')
    fetchDeviceList()
  } catch {}
}

onMounted(fetchDeviceList)
</script>

<style scoped>
.device-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.toolbar { margin-bottom: 15px; display: flex; align-items: center; gap: 10px; }
.toolbar-right { margin-left: auto; display: flex; align-items: center; gap: 6px; }
.sort-label { font-size: var(--mg-fs-sm); color: var(--mg-muted); }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>