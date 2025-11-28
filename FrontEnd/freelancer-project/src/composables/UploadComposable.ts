import { reactive, ref } from 'vue'
import type { Dataset } from '@/api/types'

export interface UploadState {
  isNewSelectedMode: boolean
  selectedDatasetName: string
  file: File | null
  options: {
    delimiter: string
    hasHeader: boolean
    timeColumn: string
    symbolColumn: string
  }
  headers: string[]
  error: string
  // Variable for UploadFileConfigPane.vue
  recordDateColumn: string
  recordDateFormat: string
  symbol: string
}

const datasets = ref<Dataset[]>([])

const uploadState = reactive<UploadState>({
    isNewSelectedMode: false,
    selectedDatasetName: '',
    file: null,
    options: {
      delimiter: ',',
      hasHeader: true,
      timeColumn: '',
      symbolColumn: '',
    },
    recordDateColumn: '',
    recordDateFormat: '',
    headers: [],
    symbol: '',
    error: ''
})

export function useUploadState() {
  return { uploadState, datasets }
}