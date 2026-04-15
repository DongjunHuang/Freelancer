import { getApiClient, API_ENDPOINTS } from '@/api/endpoints'
import type { Dataset, FetchRecordsResp, FetchDatapointsReq } from '@/types/user'


export const fetchDatasets = () => {
  return getApiClient().get<Dataset[]>(API_ENDPOINTS.dashboard.fetchDatasets)
}

export const fetchDatapoints = (datasetName: string, req: FetchDatapointsReq) => {
  return getApiClient().post<FetchRecordsResp>(
    API_ENDPOINTS.dashboard.fetchDatapoints(datasetName),
    req,
  )
}
