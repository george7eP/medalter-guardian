<template>
  <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
  >
    <el-menu-item index="0">
      <div>设备检修与预警系统</div>
    </el-menu-item>
    <el-sub-menu index="2">
      <template #title>{{ userName }}</template>
      <el-menu-item index="2-1" @click="goToProfile">个人中心</el-menu-item>
      <el-menu-item index="2-2" @click="showChangePasswordDialog">修改密码</el-menu-item>
      <el-menu-item index="2-3" @click="handleLogout">退出系统</el-menu-item>

    </el-sub-menu>
  </el-menu>

  <el-dialog
    v-model="passwordDialogVisible"
    title="修改密码"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="passwordFormRef"
      :model="passwordForm"
      :rules="passwordRules"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          type="password"
          placeholder="请输入原密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/users'
import { changePassword, logout } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const activeIndex = ref('1')

const userName = computed(() => {
  return userStore.userInfo?.realName || userStore.userInfo?.username || '用户'
})

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = reactive<FormRules>({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

function handleSelect(key: string, keyPath: string[]) {
  console.log(key, keyPath)
}

function goToProfile() {
  router.push('/user/profile') // ✨ 完美跳转到个人中心
}

function showChangePasswordDialog() {
  passwordDialogVisible.value = true
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.clearValidate()
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()

    passwordLoading.value = true

    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })

    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false

  } catch (error: any) {
    if (error !== false) {
      console.error('修改密码失败:', error)
    }
  } finally {
    passwordLoading.value = false
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // ✨ 关键修复：必须【先】呼叫后端 API，这时候 Token 还活著，后端才能成功解析并记录是谁登出的！
    try {
        await logout() 
    } catch (e) {
        console.warn('后端登出接口请求失败，仍会强制清除本地状态')
    }

    // ✨ 呼叫完后端之后，【再】清理前端状态 (删除本地 Token)
    userStore.logout()
    
    ElMessage.success('已退出系统')
    router.push('/login')

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退出失败:', error)
    }
  }
}
</script>

<style scoped>
.el-menu--horizontal > .el-menu-item:nth-child(1) {
  margin-right: auto;
}
</style>