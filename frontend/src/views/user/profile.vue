<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card class="box-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>基本資料</span>
            </div>
          </template>
          <div class="avatar-box">
            <el-avatar :size="80" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <h3>{{ userInfo.realName }}</h3>
            <el-tag type="success">系統使用者</el-tag>
          </div>
          <el-descriptions :column="1" border class="profile-desc">
            <el-descriptions-item label="登入帳號">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="手機號碼">{{ userInfo.phone || '未填寫' }}</el-descriptions-item>
            <el-descriptions-item label="電子信箱">{{ userInfo.email || '未填寫' }}</el-descriptions-item>
            <el-descriptions-item label="帳號狀態">
              <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                {{ userInfo.status === 1 ? '正常啟用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="註冊時間">{{ userInfo.createTime || '無資料' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card class="box-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>修改資料</span>
            </div>
          </template>
          <el-form :model="editForm" label-width="100px" style="max-width: 450px; padding-top: 20px;">
            <el-form-item label="登入帳號">
              <el-input v-model="editForm.username" disabled />
            </el-form-item>
            <el-form-item label="真實姓名" required>
              <el-input v-model="editForm.realName" placeholder="請輸入真實姓名" />
            </el-form-item>
            <el-form-item label="手機號碼">
              <el-input v-model="editForm.phone" placeholder="請輸入手機號碼" />
            </el-form-item>
            <el-form-item label="電子信箱">
              <el-input v-model="editForm.email" placeholder="請輸入電子郵件" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="submitLoading">儲存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from "vue"
import { ElMessage } from "element-plus"
import { getCurrentUser, updateUser } from "@/api/user"
import { useUserStore } from "@/stores/users"

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)

const userInfo = ref<any>({
  id: null, username: '', realName: '', phone: '', email: '', status: 1, createTime: ''
})

const editForm = reactive({
  id: undefined, username: '', realName: '', phone: '', email: '', status: 1
})

// 載入當前登入者資訊
const loadProfile = async () => {
  loading.value = true
  try {
    const data = await getCurrentUser()
    userInfo.value = data
    
    // 初始化同步至修改表單
    editForm.id = data.id as any
    editForm.username = data.username
    editForm.realName = data.realName
    editForm.phone = data.phone || ''
    editForm.email = data.email
    editForm.status = data.status
  } catch (error) {
    console.error("載入個人中心失敗", error)
  } finally {
    loading.value = false
  }
}

// 提交個人資料變更
const handleSave = async () => {
  if (!editForm.realName) {
    ElMessage.warning("真實姓名不能為空")
    return
  }
  submitLoading.value = true
  try {
    await updateUser(editForm.id!, editForm as any)
    ElMessage.success("個人資料更新成功！")
    
    // 異步刷新 Pinia 全局的使用者資訊快取
    userStore.setUserInfo({
      username: editForm.username,
      realName: editForm.realName
    })
    
    loadProfile()
  } catch (error) {
    console.error("儲存失敗", error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-container { padding: 20px; }
.avatar-box { text-align: center; padding: 20px 0; border-bottom: 1px dashed #e4e7ed; margin-bottom: 20px; }
.avatar-box h3 { margin: 10px 0 5px 0; color: #303133; font-size: 20px; }
.profile-desc { margin-top: 10px; }
</style>