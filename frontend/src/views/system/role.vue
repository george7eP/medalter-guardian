<template>
  <div class="role-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名稱">
          <el-input v-model="searchForm.roleName" placeholder="請輸入角色名稱" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜尋</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="roleName" label="角色名稱" min-width="150" />
        <el-table-column prop="roleCode" label="角色編碼" min-width="150" />
        <el-table-column prop="remark" label="備註" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="創建時間" width="180" align="center" />
        <el-table-column fixed="right" label="操作" width="180" align="center">
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
          @size-change="fetchRoleList"
          @current-change="fetchRoleList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="roleForm" label-width="90px">
        <el-form-item label="角色名稱" required>
          <el-input v-model="roleForm.roleName" placeholder="例如：系統管理員" />
        </el-form-item>
        <el-form-item label="角色編碼" required>
          <el-input v-model="roleForm.roleCode" :disabled="isEdit" placeholder="例如：ROLE_ADMIN" />
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="roleForm.remark" type="textarea" placeholder="請輸入角色描述說明" />
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
import { getRoleList, createRole, updateRole, deleteRole } from "@/api/system/role"
import type { RoleInfo, RoleForm } from "@/api/system/role"

const tableData = ref<RoleInfo[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const searchForm = reactive({ roleName: '' })
const pageParams = reactive({ page: 1, pageSize: 10 })

const roleForm = reactive<RoleForm>({
  id: undefined,
  roleName: '',
  roleCode: '',
  remark: ''
})

const fetchRoleList = async () => {
  loading.value = true
  try {
    const params = {
      page: pageParams.page,
      pageSize: pageParams.pageSize,
      roleName: searchForm.roleName || undefined
    }
    const result: any = await getRoleList(params)
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('獲取角色列表失敗:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchRoleList() }
const resetSearch = () => { searchForm.roleName = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  Object.assign(roleForm, { id: undefined, roleName: '', roleCode: '', remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row: RoleInfo) => {
  isEdit.value = true
  dialogTitle.value = '編輯角色'
  Object.assign(roleForm, {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    remark: row.remark
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!roleForm.roleName || !roleForm.roleCode) {
    ElMessage.warning('請填寫必填欄位')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && roleForm.id) {
      await updateRole(roleForm.id, roleForm)
      ElMessage.success('更新成功')
    } else {
      await createRole(roleForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchRoleList()
  } catch (error) {
    console.error('保存角色失敗:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: RoleInfo) => {
  try {
    await ElMessageBox.confirm(`確定要刪除角色 "${row.roleName}" 嗎？`, '警告', { type: 'error' })
    await deleteRole(row.id)
    ElMessage.success('刪除成功')
    fetchRoleList()
  } catch {}
}

onMounted(fetchRoleList)
</script>

<style scoped>
.role-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.toolbar { margin-bottom: 15px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>