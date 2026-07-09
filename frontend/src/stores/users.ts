import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const permissions = ref<string[]>([])
  const userInfo = ref<any>(null)

  const isLoggedIn = computed(() => !!token.value)
  /**
   * 设置用户认证令牌
   * @param newToken - 新的认证令牌字符串
   */

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  /**
   * 设置用户权限列表
   * @param newPermissions - 新的权限字符串数组
   */
  function setPermissions(newPermissions: string[]) {
    permissions.value = newPermissions
    localStorage.setItem('permissions', JSON.stringify(newPermissions))
  }

  function setUserInfo(info: any) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function hasPermission(permission: string): boolean {
    return permissions.value.includes(permission)
  }

  function login(loginData: { token: string; permissions: string[]; userInfo?: any }) {
    setToken(loginData.token)
    setPermissions(loginData.permissions)
    if (loginData.userInfo) {
      setUserInfo(loginData.userInfo)
    }
  }

  function logout() {
    token.value = ''
    permissions.value = []
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('permissions')
    localStorage.removeItem('userInfo')
  }

  function initFromStorage() {
    const storedToken = localStorage.getItem('token')
    const storedPermissions = localStorage.getItem('permissions')
    const storedUserInfo = localStorage.getItem('userInfo')

    if (storedToken) {
      token.value = storedToken
    }
    if (storedPermissions) {
      permissions.value = JSON.parse(storedPermissions)
    }
    if (storedUserInfo) {
      userInfo.value = JSON.parse(storedUserInfo)
    }
  }

  initFromStorage()

  return {
    token,
    permissions,
    userInfo,
    isLoggedIn,
    setToken,
    setPermissions,
    setUserInfo,
    hasPermission,
    login,
    logout,
  }
})
