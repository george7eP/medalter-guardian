import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/** 主题模式：浅色 / 深色 / 跟随系统 */
export type ThemeMode = 'light' | 'dark' | 'system'

/**
 * 主题状态管理
 * - 默认浅色；用户可切换深色或「跟随系统」
 * - 手动写入 localStorage（与 users.ts 保持一致，无持久化插件）
 * - 通过在 <html> 上切换 `dark` class 驱动 CSS 变量与 Element Plus 暗色主题
 */
export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>('light')
  const systemDark = ref(false)

  const mql = window.matchMedia('(prefers-color-scheme: dark)')

  /** 实际生效的主题（把 system 解析为 light/dark） */
  const resolved = computed<'light' | 'dark'>(() =>
    mode.value === 'system' ? (systemDark.value ? 'dark' : 'light') : mode.value,
  )

  function apply() {
    document.documentElement.classList.toggle('dark', resolved.value === 'dark')
  }

  function setMode(newMode: ThemeMode) {
    mode.value = newMode
    localStorage.setItem('theme', newMode)
    apply()
  }

  function initFromStorage() {
    const stored = localStorage.getItem('theme')
    if (stored === 'light' || stored === 'dark' || stored === 'system') {
      mode.value = stored
    }
    systemDark.value = mql.matches
    // 系统偏好变化时，仅在「跟随系统」模式下实时切换
    mql.addEventListener('change', (e) => {
      systemDark.value = e.matches
      if (mode.value === 'system') apply()
    })
    apply()
  }

  initFromStorage()

  return {
    mode,
    resolved,
    setMode,
  }
})
