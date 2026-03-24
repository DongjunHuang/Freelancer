import { reactive, ref } from 'vue'
import type { Dataset, FetchRecordsResp } from '@/types/user'

export interface DashboardFilters {
  selectedDatasetName: string
  selectedColumns: string[]
  startDate: string
  endDate: string
  symbols: string
}

const filters = reactive<DashboardFilters>({
  selectedDatasetName: '',
  selectedColumns: [],
  startDate: '',
  endDate: '',
  symbols: '',
})

const datasets = ref<Dataset[]>([])
const records = ref<FetchRecordsResp>()

export function useDashboardState() {
  return { filters, datasets, records }
}
