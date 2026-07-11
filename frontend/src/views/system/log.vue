<template>
  <div class="log-container">
    <PageHeader title="日志管理" subtitle="系统操作审计日志" />
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入帐号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜寻</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="操作人" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.username }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作描述" width="150" />
        <el-table-column prop="method" label="请求方法" min-width="200" show-overflow-tooltip />
        <el-table-column prop="params" label="传入参数" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="130" align="center" />
        <el-table-column prop="time" label="耗时(ms)" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.time > 500 ? 'danger' : 'success'">{{ row.time }} ms</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" align="center" />
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="fetchLogList"
          @current-change="fetchLogList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import { ref, reactive, onMounted } from "vue"
import { getLogList } from "@/api/system/log"

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const searchForm = reactive({ username: '' })
const pageParams = reactive({ page: 1, pageSize: 15 })

const fetchLogList = async () => {
  loading.value = true
  try {
    const result: any = await getLogList({ ...pageParams, username: searchForm.username || undefined })
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchLogList() }
const resetSearch = () => { searchForm.username = ''; handleSearch() }

onMounted(fetchLogList)
</script>

<style scoped>
.log-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>