import request from '@/util/request'

export interface RoleInfo {
    id: number
    roleCode: string // 角色编码
    roleName: string // 角色名称
    remark?: string  // 备注
    createTime?: string
}

export interface RoleForm {
    id?: number
    roleCode: string
    roleName: string
    remark?: string
}

// 获取角色分页列表
export function getRoleList(params?: any) {
    return request.get('/role', { params })
}

// 创建新角色
export function createRole(data: RoleForm) {
    return request.post('/role', data)
}

// 更新角色信息
export function updateRole(id: number, data: RoleForm) {
    return request.put(`/role/${id}`, data)
}

// 删除角色
export function deleteRole(id: number) {
    return request.delete(`/role/${id}`)
}