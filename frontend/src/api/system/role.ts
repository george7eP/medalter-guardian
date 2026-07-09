import request from '@/util/request'

export interface RoleInfo {
    id: number
    roleCode: string // 角色編碼
    roleName: string // 角色名稱
    remark?: string  // 備註
    createTime?: string
}

export interface RoleForm {
    id?: number
    roleCode: string
    roleName: string
    remark?: string
}

// 獲取角色分頁列表
export function getRoleList(params?: any) {
    return request.get('/role', { params })
}

// 創建新角色
export function createRole(data: RoleForm) {
    return request.post('/role', data)
}

// 更新角色信息
export function updateRole(id: number, data: RoleForm) {
    return request.put(`/role/${id}`, data)
}

// 刪除角色
export function deleteRole(id: number) {
    return request.delete(`/role/${id}`)
}