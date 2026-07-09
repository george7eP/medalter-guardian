<template>
  <div class="user-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="使用者名稱">
          <el-input v-model="searchForm.username" placeholder="請輸入名稱" clearable />
        </el-form-item>
        <el-form-item label="狀態">
          <el-select v-model="searchForm.status" placeholder="請選擇" clearable style="width: 150px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
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
        <el-button type="primary" @click="handleAdd">添加用戶</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="登入帳號" min-width="120" />
        <el-table-column prop="realName" label="真實姓名" min-width="120" />
        <el-table-column prop="phone" label="手機號碼" width="150" align="center" />
        <el-table-column prop="email" label="電子信箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="狀態" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">編輯</el-button>
            <el-button link type="warning" size="small" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchUserList"
          @current-change="fetchUserList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="帳號">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="真實姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="密碼" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="手機號碼">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="電子信箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="狀態">
          <el-radio-group v-model="userForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">確定保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配用戶角色" width="450px" destroy-on-close>
      <el-form label-width="80px" style="padding: 10px 20px 0 10px;">
        <el-form-item label="登入帳號">
          <el-input :value="currentRoleUser.username" disabled />
        </el-form-item>
        <el-form-item label="真實姓名">
          <el-input :value="currentRoleUser.realName" disabled />
        </el-form-item>
        <el-form-item label="選擇角色">
          <el-select v-model="selectedRoleIds" multiple placeholder="請選擇角色" style="width: 100%">
            <el-option
              v-for="role in allRoles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignRole" :loading="roleSubmitLoading">確定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getUserList, deleteUser, createUser, updateUser, getUserRoleIds, assignUserRoles } from "@/api/user"
import type { UserInfo, UserForm } from "@/api/user"
import { getRoleList } from "@/api/system/role"

const tableData = ref<UserInfo[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const searchForm = reactive({ username: '', status: undefined as number | undefined })
const pageParams = reactive({ page: 1, pageSize: 10 })

const userForm = reactive<UserForm>({
  id: undefined,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  status: 1
})

// ✨ 新增：分配角色相關狀態變數
const roleDialogVisible = ref(false)
const roleSubmitLoading = ref(false)
const allRoles = ref<any[]>([])
const selectedRoleIds = ref<number[]>([])
const currentRoleUser = ref<any>({})

const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pageParams.page, 
      pageSize: pageParams.pageSize,
      username: searchForm.username || undefined,
      status: searchForm.status
    }
    const result: any = await getUserList(params)
    tableData.value = result.records || result.list || []
    total.value = result.total || 0
  } catch (error) {
    console.error('獲取列表失敗:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageParams.page = 1; fetchUserList() }
const resetSearch = () => { searchForm.username = ''; searchForm.status = undefined; handleSearch() }

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用戶'
  Object.assign(userForm, { id: undefined, username: '', password: '', realName: '', email: '', phone: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: UserInfo) => {
  isEdit.value = true
  dialogTitle.value = '編輯用戶'
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    realName: row.realName || '',
    email: row.email || '',
    phone: row.phone || '',
    status: row.status
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  submitLoading.value = true
  try {
    if (isEdit.value && userForm.id) {
      await updateUser(userForm.id, userForm)
      ElMessage.success('更新成功')
    } else {
      await createUser(userForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error('保存失敗', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm(`確定要刪除用戶 "${row.username}" 嗎？`, '警告', { type: 'error' })
    await deleteUser(row.id)
    ElMessage.success('刪除成功')
    fetchUserList()
  } catch {}
}

// ✨ 新增：點擊開啟分配角色彈窗
const handleAssignRole = async (row: UserInfo) => {
  currentRoleUser.value = row
  selectedRoleIds.value = []
  roleDialogVisible.value = true
  
  try {
    // 1. 同步加載系統內所有的角色下拉選項
    const roleRes: any = await getRoleList({ page: 1, pageSize: 1000 })
    allRoles.value = roleRes.records || roleRes.list || []
    
    // 2. 異步請求該用戶當前已擁有的角色 ID 列表
    const activeIds = await getUserRoleIds(row.id!)
    selectedRoleIds.value = activeIds
  } catch (error) {
    console.error("獲取角色數據失敗:", error)
  }
}

// ✨ 新增：提交角色分配數據
const submitAssignRole = async () => {
  if (!currentRoleUser.value.id) return
  roleSubmitLoading.value = true
  try {
    await assignUserRoles(currentRoleUser.value.id, selectedRoleIds.value)
    ElMessage.success("角色及權限分配成功")
    roleDialogVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error("分配角色失敗:", error)
  } finally {
    roleSubmitLoading.value = false
  }
}

onMounted(fetchUserList)
</script>

<style scoped>
.user-container { padding: 10px; }
.search-card { margin-bottom: 15px; }
.table-card { min-height: 500px; }
.toolbar { margin-bottom: 15px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>