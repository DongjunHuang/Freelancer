import http from './http'

// Test Insert metric to mysql
export const insertMetricMysql = (value: number) => http.post('/tests/insert', { value });

// Test Fetch number of items in mysql metric tabkles
export const fetchCountMysql = () => http.get('/tests/getNumber')

// Test Insert metric to mongodb
export const insertMetricNosql = (value: number) => http.post('/tests/insertNosql', { value })

// Test Fetch the number of metrics from mongo db
export const fetchCountNosql = () => http.get('/tests/getNumberNosql')

// Test sending email to clients locally
export const sendEmailTest = () => http.get('/tests/sendEmailTest')
