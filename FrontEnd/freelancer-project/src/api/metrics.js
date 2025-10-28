import http from './http'

export const insertMetricMysql = (value) => http.post('/metrics/insert', { value })
export const fetchCountMysql = (value) => http.get('/metrics/getNumber', { value })
export const insertMetricNosql = (value) => http.post('/metrics/insertNosql', { value })
export const fetchCountNosql = (value) => http.get('/metrics/getNumberNosql', { value })
