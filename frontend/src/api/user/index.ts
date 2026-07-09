import request from '@/util/request'

// --- 授權與登入相關介面 ---
export interface Permission {
  apiUrl: string
  id: number
  parentId: number
  path: string | null
  permCode: string
  permName: string
  permType: string
  remark: string | null
  sort: number
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  username: string
  realName: string
  permissions: Permission[]
}

// --- 用戶管理相關介面 (嚴格對齊資料庫) ---
export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone?: string
  status: number
  createTime?: string
}

export interface UserForm {
  id?: number
  username: string
  password?: string
  realName: string
  email: string
  phone?: string
  status?: number
}

// --- API 請求函數 ---

// 1. 登入與基礎操作
export function login(data: LoginParams) {
  return request.post<LoginResult>('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/user/password', data)
}

export function getCurrentUser() {
  return request.get<UserInfo>('/user/info')
}

// 2. 用戶 CRUD 操作
export function getUserList(params?: any) {
  return request.get('/user', { params })
}

export function createUser(data: UserForm) {
  return request.post('/user', data)
}

export function updateUser(id: number, data: UserForm) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/user/${id}`)
}

/**
 * ✨ 新增：獲取指定用戶擁有的角色 ID 列表
 */
export function getUserRoleIds(userId: number) {
  return request.get<number[]>(`/user/${userId}/roles`)
}

/**
 * ✨ 新增：給指定用戶分配多個角色
 */
export function assignUserRoles(userId: number, roleIds: number[]) {
  return request.put(`/user/${userId}/roles`, roleIds)
}