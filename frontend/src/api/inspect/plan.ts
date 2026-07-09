import request from '@/util/request'

export interface InspectPlan {
    id?: number
    deviceId: number | undefined
    planDate: string
    inspectContent: string
    principal: string
    planStatus: 'PENDING' | 'COMPLETED'
    remark?: string
    createTime?: string
}

export function getPlanList(params: any) {
    return request.get('/inspect/plan', { params })
}

export function createPlan(data: InspectPlan) {
    return request.post('/inspect/plan', data)
}

export function updatePlan(id: number, data: InspectPlan) {
    return request.put(`/inspect/plan/${id}`, data)
}

export function deletePlan(id: number) {
    return request.delete(`/inspect/plan/${id}`)
}