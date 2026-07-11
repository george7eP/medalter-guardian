import request from '@/util/request'

export interface WarnRule {
    id?: number
    deviceId?: number | null  // 若为 null 代表是全局规则
    warnCondition: string
    warnLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
    notifyType: string
    ruleStatus: number // 1 启用, 0 禁用
    remark?: string
    createTime?: string
    updateTime?: string
}

export function getRuleList(params: any) {
    return request.get('/warn/rule', { params })
}

export function createRule(data: WarnRule) {
    return request.post('/warn/rule', data)
}

export function updateRule(id: number, data: WarnRule) {
    return request.put(`/warn/rule/${id}`, data)
}

export function deleteRule(id: number) {
    return request.delete(`/warn/rule/${id}`)
}