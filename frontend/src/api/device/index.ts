import request from '@/util/request'

export interface DeviceInfo {
    id?: number
    deviceName: string
    deviceModel?: string
    manufacturer?: string
    purchaseDate?: string
    useDepartment: string
    inspectCycle: number
    warnDays: number
    lastInspectDate?: string
    deviceStatus: 'NORMAL' | 'WARN' | 'FAULT'
    remark?: string
    createTime?: string
    updateTime?: string
}

export interface DeviceListParams {
    page: number
    pageSize: number
    deviceName?: string
    deviceStatus?: string
}

// 获取设备分页列表
export function getDeviceList(params: DeviceListParams) {
    return request.get('/device', { params })
}

// 新增设备
export function createDevice(data: DeviceInfo) {
    return request.post('/device', data)
}

// 修改设备
export function updateDevice(id: number, data: DeviceInfo) {
    return request.put(`/device/${id}`, data)
}

// 删除设备
export function deleteDevice(id: number) {
    return request.delete(`/device/${id}`)
}