import http from './http'

export const refreshAccessTokenRequest = () => http.get('/dashboard/getNumberSql')