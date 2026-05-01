import { getApiClient, API_ENDPOINTS } from '@/api/endpoints'
import { AxiosProgressEvent, AxiosRequestConfig } from 'axios'
import type {
  GetUserDatasetsResp,
  QueryRecordsResp,
  QueryRecordsReq,
  CreateDatasetReq,
  CreateDatasetResp,
} from '@/types/dataset'

export interface CreateDatasetOptions {
  onProgress?: (pct: number) => void

  signal?: AbortSignal
}

const toJsonBlob = (value: unknown) =>
  new Blob([JSON.stringify(value)], { type: 'application/json' })

const calcUploadPercent = (event: AxiosProgressEvent): number | null => {
  if (!event.total || event.total <= 0) {
    return null
  }
  const pct = Math.round((event.loaded / event.total) * 100)
  return Math.max(0, Math.min(100, pct))
}

// ================================== APIs ====================================
export const createDataset = (
  file: File,
  dataset: CreateDatasetReq,
  options?: CreateDatasetOptions,
) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dataset', toJsonBlob(dataset))
  const requestConfig: AxiosRequestConfig = {
    signal: options?.signal,
    onUploadProgress: (event) => {
      const pct = calcUploadPercent(event)
      if (pct !== null) {
        options?.onProgress?.(pct)
      }
    },
  }

  return getApiClient().post<CreateDatasetResp>(
    API_ENDPOINTS.dataset.createDataset,
    formData,
    requestConfig,
  )
}

export const getUserDatasets = () => {
  return getApiClient().get<GetUserDatasetsResp>(API_ENDPOINTS.dataset.getUserDatasets)
}

export const queryRecords = (datasetId: string, req: QueryRecordsReq) => {
  return getApiClient().post<QueryRecordsResp>(API_ENDPOINTS.dataset.queryRecords(datasetId), req)
}
