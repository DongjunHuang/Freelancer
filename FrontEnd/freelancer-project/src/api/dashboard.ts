import http from './http'

export const refreshAccessTokenRequest = () => http.get(`/dashboard/getNumberSql`)

export const fetchDatasets = () => http.get(`/dashboard/fetchDatasets`)