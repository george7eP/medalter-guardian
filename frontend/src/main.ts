import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/theme.css'

import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 初始化主题（读取偏好并应用 html.dark，绑定系统偏好监听）
useThemeStore(pinia)

app.mount('#app')
