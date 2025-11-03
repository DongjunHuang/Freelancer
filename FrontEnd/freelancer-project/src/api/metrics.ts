import http from './http'

// Insert metric to mysql
export const insertMetricMysql = (value: number) => http.post('/metrics/insert', { value });

// Fetch number of items in mysql metric tabkles
export const fetchCountMysql = () => http.get('/metrics/getNumber')

// Insert metric to mongodb
export const insertMetricNosql = (value: number) => http.post('/metrics/insertNosql', { value })

// Fetch the number of metrics from mongo db
export const fetchCountNosql = () => http.get('/metrics/getNumberNosql')

