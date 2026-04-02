import { getApiClient, API_ENDPOINTS } from '@/api/endpoints'

// Test Insert metric to mysql
export const insertMetricMysql = (value: number) =>
  getApiClient().post(API_ENDPOINTS.tests.insertMetricMysql, { value })

// Test Fetch number of items in mysql metric tabkles
export const fetchCountMysql = () => getApiClient().get(API_ENDPOINTS.tests.fetchCountMysql)

// Test Insert metric to mongodb
export const insertMetricNosql = (value: number) =>
  getApiClient().post(API_ENDPOINTS.tests.fetchCountNosql, { value })

// Test Fetch the number of metrics from mongo db
export const fetchCountNosql = () => getApiClient().get(API_ENDPOINTS.tests.fetchCountNosql)

// Test sending email to clients locally
export const sendEmailTest = () => getApiClient().get(API_ENDPOINTS.tests.sendEmailTest)
