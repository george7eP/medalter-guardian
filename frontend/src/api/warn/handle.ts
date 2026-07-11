import request from '@/util/request'

export interface WarnInfo {
    id?: number
    deviceId: number
    warnLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
    warnContent: string
    warnTime?: string
    handleStatus: 'UNHANDLED' | 'PROCESSED' | 'IGNORED'
    handleUser?: string
    handleTime?: string
    handleRemark?: string
}

export function getWarnList(params: any) {
    return request.get('/warn/info', { params })
}

// 提交处理结果
export function handleWarn(id: number, data: Partial<WarnInfo>) {
    return request.put(`/warn/info/handle/${id}`, data)
}

export function deleteWarn(id: number) {
    return request.delete(`/warn/info/${id}`)
}