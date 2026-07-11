<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Monitor, AlarmClock, Key } from '@element-plus/icons-vue'
import { login } from '@/api/user'
import { useUserStore } from '@/stores/users'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, message: '密码长度不能小于3位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }

  loading.value = true

  try {
    const result = await login(loginForm)

    userStore.login({
      token: result.token,
      permissions: result.permissions.map(p => p.permCode),
      userInfo: {
        username: result.username,
        realName: result.realName
      }
    })

    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

function handleKeyPress(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    handleLogin()
  }
}

const features = [
  { icon: Monitor, text: '设备全生命周期检修管理' },
  { icon: AlarmClock, text: '到期自动预警，防患未然' },
  { icon: Key, text: '细粒度权限，操作全程留痕' },
]
</script>

<template>
  <div class="login-page" @keypress="handleKeyPress">
    <!-- 品牌面板 -->
    <aside class="brand-panel">
      <div class="brand-inner">
        <div class="brand-top">
          <span class="logo">
            <svg viewBox="0 0 32 32" width="30" height="30">
              <path d="M16 2.5 27 6.2v8.3c0 6.6-4.4 12.4-11 15-6.6-2.6-11-8.4-11-15V6.2L16 2.5Z"
                fill="rgba(255,255,255,.18)" stroke="#fff" stroke-width="1.2" />
              <path d="M7.5 16.5h4l1.8-4.2 3 8.4 2-4.2h5.7" fill="none" stroke="#fff"
                stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </span>
          <div class="logo-text">
            <b>医疗设备守卫</b>
            <span>MEDALTER GUARDIAN</span>
          </div>
        </div>

        <div class="brand-copy">
          <h1>让每一台设备<br />都在最佳状态运转</h1>
          <p>检修计划、到期预警、处理闭环，一体化守护医疗设备安全。</p>
        </div>

        <ul class="brand-features">
          <li v-for="(f, i) in features" :key="i" :style="{ animationDelay: 120 * i + 200 + 'ms' }">
            <span class="fi"><el-icon><component :is="f.icon" /></el-icon></span>{{ f.text }}
          </li>
        </ul>

        <!-- 心电波形装饰 -->
        <svg class="ecg" viewBox="0 0 600 80" preserveAspectRatio="none" aria-hidden="true">
          <path d="M0 40 H120 l14 -26 l16 52 l14 -40 l12 22 H340 l14 -30 l16 44 l12 -14 H600"
            fill="none" stroke="rgba(255,255,255,.55)" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </div>
    </aside>

    <!-- 表单面板 -->
    <main class="form-panel">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>登录以继续管理您的设备</p>
        </div>

        <el-form :model="loginForm" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="submit-btn"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="foot-note">医疗设备检修与预警系统 · 请使用授权账号登录</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--mg-bg);
}

/* —— 左侧品牌面板 —— */
.brand-panel {
  position: relative;
  flex: 1.1;
  overflow: hidden;
  background:
    radial-gradient(120% 120% at 0% 0%, #12B5B5 0%, #0B8686 45%, #0A6E78 100%);
  color: #fff;
}
.brand-panel::before {
  /* 柔和光斑 */
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(400px 400px at 85% 15%, rgba(255,255,255,.18), transparent 60%),
    radial-gradient(300px 300px at 20% 90%, rgba(30,111,217,.28), transparent 60%);
}
.brand-inner {
  position: relative;
  height: 100%;
  padding: 56px 60px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}
.brand-top {
  display: flex;
  align-items: center;
  gap: 13px;
}
.logo {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(255, 255, 255, .12);
  border: 1px solid rgba(255, 255, 255, .25);
}
.logo-text { display: flex; flex-direction: column; line-height: 1.25; }
.logo-text b { font-size: 19px; letter-spacing: 1px; }
.logo-text span { font-size: 11px; opacity: .7; letter-spacing: 2px; }

.brand-copy { margin-top: auto; }
.brand-copy h1 {
  font-size: 38px;
  line-height: 1.28;
  font-weight: 700;
  margin: 0 0 16px;
  letter-spacing: 1px;
  animation: mg-rise .6s var(--mg-ease) both;
}
.brand-copy p {
  font-size: 15px;
  opacity: .82;
  max-width: 380px;
  margin: 0;
  animation: mg-rise .6s var(--mg-ease) both;
  animation-delay: 120ms;
}
.brand-features {
  list-style: none;
  padding: 0;
  margin: 30px 0 auto;
  display: flex;
  flex-direction: column;
  gap: 13px;
}
.brand-features li {
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 14px;
  opacity: .95;
  animation: mg-rise .6s var(--mg-ease) both;
}
.fi {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: rgba(255, 255, 255, .14);
  color: #fff;
  font-size: 16px;
}
.ecg {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 34px;
  width: 100%;
  height: 70px;
  opacity: .5;
}
.ecg path {
  stroke-dasharray: 1400;
  stroke-dashoffset: 1400;
  animation: ecg-draw 3.4s var(--mg-ease) .4s forwards;
}
@keyframes ecg-draw { to { stroke-dashoffset: 0; } }

/* —— 右侧表单 —— */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.login-card {
  width: 100%;
  max-width: 380px;
  animation: mg-rise .5s var(--mg-ease) both;
  animation-delay: 120ms;
}
.login-header { margin-bottom: 30px; }
.login-header h2 {
  font-size: 28px;
  color: var(--mg-ink);
  margin: 0 0 8px;
  font-weight: 700;
}
.login-header p {
  font-size: 14px;
  color: var(--mg-muted);
  margin: 0;
}
.login-form :deep(.el-input__wrapper) {
  padding: 8px 14px;
  border-radius: 10px;
}
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 10px;
  margin-top: 6px;
}
.foot-note {
  margin-top: 26px;
  text-align: center;
  font-size: 12px;
  color: var(--mg-muted);
}

/* —— 响应式：窄屏隐藏品牌面板 —— */
@media (max-width: 860px) {
  .brand-panel { display: none; }
}
</style>
