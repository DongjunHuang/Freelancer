import http from './http'

export const insertMetric = (value) => http.post('/metrics/insert', { value })
export const fetchCount = (value) => http.get('/metrics/getNumber', { value })
