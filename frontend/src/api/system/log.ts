import request from '@/util/request'

export function getLogList(params: any) {
    return request.get('/system/log', { params })
}