import http from './http'
import type { Dataset, FetchRecordsResp } from '@/api/types'

export const refreshAccessTokenRequest = () => http.get(`/dashboard/getNumberSql`)

export async function fetchDatasets(): Promise<Dataset[]> {
    const res = await http.get<Dataset[]>('/dashboard/fetchDatasets') 
    return res.data
}

export interface FetchRecordsParams {
    datasetName: string
    startDate: string
    endDate: string
    columns: string[]
    symbols: string
  }

export const fetchDatapoints = (params: FetchRecordsParams) => {
return http.post<FetchRecordsResp>(
    '/dashboard/queryDatapoints', 
    params
)
}