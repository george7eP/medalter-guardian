<template>
  <div class="warn-handle-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="關聯設備">
          <el-select v-model="searchForm.deviceId" placeholder="選擇設備過濾" clearable filterable style="width: 200px">
            <el-option v-for="dev in deviceOptions" :key="dev.id" :label="dev.deviceName" :value="dev.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="處理狀態">
          <el-select v-model="searchForm.handleStatus" placeholder="選擇狀態" clearable style="width: 150px">
            <el-option label="未處理" value="UNHANDLED" />
            <el-option label="已處理" value="PROCESSED" />
            <el-option label="已忽略" value="IGNORED" />
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
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="關聯設備" min-width="140">
          <template #default="{ row }">
            {{ getDeviceName(row.deviceId) }}
          </template>
        </el-table-column>
        <el-table-column prop="warnLevel" label="級別" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.warnLevel)" effect="dark">
              {{ levelText(row.warnLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warnContent" label="預警內容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="warnTime" label="預警時間" width="160" align="center" />
        
        <el-table-column prop="handleStatus" label="處理狀態" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.handleStatus)">
              {{ statusText(row.handleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleUser" label="處理人" width="100" align="center" />
        
        <el-table-column fixed="right" label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.handleStatus === 'UNHANDLED'" 
              link type="primary" size="small" 
              @click="openHandleDialog(row)"
            >
              去處理
            </el-button>
            <el-button 
              v-else 
              link type="info" size="small" 
              @click="openHandleDialog(row, true)"
            >
              查看
            </el-button>
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
          @size-change="fetchWarnList"
          @current-change="fetchWarnList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isViewOnly ? '預警處理詳情' : '處理預警'" width="500px" destroy-on-close>
      <el-form :model="handleForm" label-width="90px">
        <el-form-item label="預警內容">
          <el-alert :title="currentWarn?.warnContent" :type="levelTagType(currentWarn?.warnLevel || 'LOW')" :closable="false" />
        </el-form-item>
        <el-form-item label="預警時間">
          <span>{{ currentWarn?.warnTime }}</span>
        </el-form-item>
        
        <el-divider border-style="dashed" />

        <el-form-item label="處理結果" required>
          <el-radio-group v-model="handleForm.handleStatus" :disabled="isViewOnly">
            <el-radio value="PROCESSED">已處理</el-radio>
            <el-radio value="IGNORED">忽略警報</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="處理人" required>
          <el-input v-model="handleForm.handleUser" :disabled="isViewOnly" placeholder="請輸入處理人姓名" />
        </el-form-item>
        <el-form-item label="處理備註" :required="handleForm.handleStatus === 'PROCESSED'">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" :disabled="isViewOnly" placeholder="請填寫具體的排查或處理措施" />
        </el-form-item>
      </el-form>
      <template #footer v-if="!isViewOnly">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="submitLoading">提交處理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { getWarnList, handleWarn } from "@/api/warn/handle"
import type { WarnInfo } from "@/api/warn/handle"
import { getDeviceList } from "@/api/device" 
import type { DeviceInfo } from "@/api/device"
import { useUserStore } from '@/stores/users'

const userStore = useUserStore()
const tableData = ref<WarnInfo[]>([])
const deviceOptions = ref<DeviceInfo[]>([]) 
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)

const dialogVisible = ref(false)
const isViewOnly = ref(false)
const currentWarn = ref<WarnInfo | null>(null)

const searchForm = reactive({ deviceId: undefined as number | undefined, warnLevel: '', handleStatus: '' })
const pageParams = reactive({ page: 1, pageSize: 10 })

const handleForm = reactive({
  id: undefined as number | undefined,
  handleStatus: 'PROCESSED' as 'PROCESSED' | 'IGNORED',
  handleUser: '',
  handleRemark: ''
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
  return 'danger' // HIGH, URGENT
}

const levelText = (level: string) => {
  if (level === 'LOW') return '低'
  if (level === 'MEDIUM') return '中'
  if (level === 'HIGH') return '高'
  if (level === 'URGENT') return '緊急'
  return level
}

const statusTagType = (status: string) => {
  if (status === 'UNHANDLED') return 'danger'
  if (status === 'PROCESSED') return 'success'
  return 'info' // IGNORED
}

const statusText = (status: string) => {
  if (status === 'UNHANDLED') return '未處理'
  if (status === 'PROCESSED') return '已處理'
  return '已忽略'
}

const fetchWarnList = async () => {
  loading.value = true
  try {
    const params = {
      page: pageParams.page,
      pageSize: pageParams.pageSize,
      deviceId: searchForm.deviceId,
      warnLevel: searchForm.warnLevel || undefined,
      handleStatus: searchForm.handleStatus || undefined
    }
    const result: any = await getWarnList(params)
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('獲取預警列表失敗:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchWarnList() }
const resetSearch = () => { searchForm.deviceId = undefined; searchForm.warnLevel = ''; searchForm.handleStatus = ''; handleSearch() }

const openHandleDialog = (row: WarnInfo, viewOnly = false) => {
  isViewOnly.value = viewOnly
  currentWarn.value = row
  
  if (viewOnly) {
    handleForm.handleStatus = row.handleStatus as any
    handleForm.handleUser = row.handleUser || ''
    handleForm.handleRemark = row.handleRemark || ''
  } else {
    handleForm.id = row.id
    handleForm.handleStatus = 'PROCESSED'
    // 預設填入當前登入者名稱
    handleForm.handleUser = userStore.userInfo?.realName || userStore.userInfo?.username || ''
    handleForm.handleRemark = ''
  }
  
  dialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.handleUser) {
    ElMessage.warning('請填寫處理人')
    return
  }
  if (handleForm.handleStatus === 'PROCESSED' && !handleForm.handleRemark) {
    ElMessage.warning('標記為「已處理」時，請填寫處理備註')
    return
  }
  
  submitLoading.value = true
  try {
    await handleWarn(handleForm.id!, {
      handleStatus: handleForm.handleStatus,
      handleUser: handleForm.handleUser,
      handleRemark: handleForm.handleRemark
    })
    ElMessage.success('處理提交成功')
    dialogVisible.value = false
    fetchWarnList()
  } catch (error) {
    console.error('處理失敗:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchDeviceOptions().then(() => {
    fetchWarnList()
  })
})
</script>

<style scoped>
.warn-handle-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>