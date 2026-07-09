import request from '@/util/request'

export interface InspectRecord {
    id?: number
    deviceId: number | undefined
    inspectDate: string
    inspectContent: string
    inspectResult: 'PASS' | 'PARTIAL' | 'FAIL'
    reportFile?: string
    operator: string
    remark?: string
    createTime?: string
}

export function getRecordList(params: any) {
    return request.get('/inspect/record', { params })
}

export function createRecord(data: InspectRecord) {
    return request.post('/inspect/record', data)
}

export function updateRecord(id: number, data: InspectRecord) {
    return request.put(`/inspect/record/${id}`, data)
}

export function deleteRecord(id: number) {
    return request.delete(`/inspect/record/${id}`)
}