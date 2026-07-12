<template>
  <div class="app-header">
    <!-- 品牌 -->
    <div class="brand" @click="goHome">
      <span class="brand-mark" aria-hidden="true">
        <svg viewBox="0 0 32 32" width="22" height="22">
          <path
            d="M16 2.5 27 6.2v8.3c0 6.6-4.4 12.4-11 15-6.6-2.6-11-8.4-11-15V6.2L16 2.5Z"
            fill="url(#mgShield)" />
          <path
            d="M7.5 16.5h4l1.8-4.2 3 8.4 2-4.2h5.7"
            fill="none" stroke="#fff" stroke-width="1.8"
            stroke-linecap="round" stroke-linejoin="round" />
          <defs>
            <linearGradient id="mgShield" x1="5" y1="3" x2="27" y2="29">
              <stop offset="0" stop-color="#12B5B5" />
              <stop offset="1" stop-color="#0B8686" />
            </linearGradient>
          </defs>
        </svg>
      </span>
      <span class="brand-text">
        <span class="brand-title">医疗设备守卫</span>
        <span class="brand-sub">检修 · 预警管理平台</span>
      </span>
    </div>

    <div class="spacer" />

    <!-- 主题切换 -->
    <el-dropdown trigger="click" @command="onThemeCommand" class="theme-dropdown">
      <button class="theme-toggle" type="button" :title="'主题：' + themeLabel">
        <el-icon><component :is="themeIcon" /></el-icon>
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="opt in themeOptions" :key="opt.value"
            :command="opt.value"
            :class="{ 'is-active-theme': themeStore.mode === opt.value }"
          >
            <el-icon class="theme-item-icon"><component :is="opt.icon" /></el-icon>
            {{ opt.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 用户区 -->
    <el-dropdown trigger="click" @command="onCommand">
      <div class="user-chip">
        <span class="avatar">{{ avatarText }}</span>
        <span class="user-name">{{ userName }}</span>
        <el-icon class="caret"><ArrowDown /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile" :icon="UserIcon">个人中心</el-dropdown-item>
          <el-dropdown-item command="password" :icon="Lock">修改密码</el-dropdown-item>
          <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出系统</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>

  <el-dialog
    v-model="passwordDialogVisible"
    title="修改密码"
    width="460px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="passwordFormRef"
      :model="passwordForm"
      :rules="passwordRules"
      label-width="88px"
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
import {
  ArrowDown,
  Lock,
  SwitchButton,
  User as UserIcon,
  Sunny,
  Moon,
  Monitor,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/users'
import { useThemeStore, type ThemeMode } from '@/stores/theme'
import { changePassword, logout } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const themeOptions = [
  { value: 'light' as ThemeMode, label: '浅色', icon: Sunny },
  { value: 'dark' as ThemeMode, label: '深色', icon: Moon },
  { value: 'system' as ThemeMode, label: '跟随系统', icon: Monitor },
]

const themeLabel = computed(
  () => themeOptions.find(o => o.value === themeStore.mode)?.label || '浅色',
)
// 按钮图标反映实际生效的主题（system 解析为日/月）
const themeIcon = computed(() => {
  if (themeStore.mode === 'system') return Monitor
  return themeStore.resolved === 'dark' ? Moon : Sunny
})

function onThemeCommand(mode: ThemeMode) {
  themeStore.setMode(mode)
}

const userName = computed(() => {
  return userStore.userInfo?.realName || userStore.userInfo?.username || '用户'
})

const avatarText = computed(() => userName.value.trim().charAt(0) || '用')

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

function goHome() {
  router.push('/')
}

function onCommand(command: string) {
  if (command === 'profile') goToProfile()
  else if (command === 'password') showChangePasswordDialog()
  else if (command === 'logout') handleLogout()
}

function goToProfile() {
  router.push('/user/profile')
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

    // 先呼叫后端 API（此时 Token 仍有效，后端才能记录登出者），再清理前端状态
    try {
      await logout()
    } catch (e) {
      console.warn('后端登出接口请求失败，仍会强制清除本地状态')
    }

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
.app-header {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px 0 18px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  cursor: pointer;
  user-select: none;
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: var(--mg-primary-soft);
  transition: transform var(--mg-dur) var(--mg-ease);
}
.brand:hover .brand-mark {
  transform: translateY(-1px) rotate(-4deg);
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}
.brand-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--mg-ink);
  letter-spacing: .5px;
}
.brand-sub {
  font-size: 11px;
  color: var(--mg-muted);
  letter-spacing: .5px;
}

.spacer { flex: 1; }

/* 主题切换按钮 */
.theme-dropdown { margin-right: 6px; }
.theme-toggle {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--mg-text);
  font-size: 18px;
  cursor: pointer;
  outline: none;
  transition: background-color var(--mg-dur-fast) var(--mg-ease),
    color var(--mg-dur-fast) var(--mg-ease);
}
.theme-toggle:hover {
  background: var(--mg-surface-2);
  color: var(--mg-primary);
}
.theme-item-icon { margin-right: 8px; vertical-align: -2px; }
.is-active-theme {
  color: var(--mg-primary);
  font-weight: 600;
  background: var(--mg-primary-soft);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 5px 10px 5px 5px;
  border-radius: 999px;
  cursor: pointer;
  outline: none;
  transition: background-color var(--mg-dur-fast) var(--mg-ease);
}
.user-chip:hover {
  background: var(--mg-surface-2);
}
.avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #12B5B5, #0B8686);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}
.user-name {
  font-size: 14px;
  color: var(--mg-ink);
  font-weight: 500;
}
.caret {
  font-size: 12px;
  color: var(--mg-muted);
}
</style>
