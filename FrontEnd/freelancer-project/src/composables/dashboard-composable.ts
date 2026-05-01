import { reactive, ref } from 'vue'
import type { Dataset, QueryRecordsResp } from '@/types/dataset'

const datasets = ref<Dataset[]>([])
const records = ref<QueryRecordsResp>()

export interface DashboardFilters {
  selectedDatasetName: string
  selectedDatasetId: string | null
  selectedColumns: string[]
  startDate: string
  endDate: string
  symbols: string
}

const filters = reactive<DashboardFilters>({
  selectedDatasetName: '',
  selectedColumns: [],
  selectedDatasetId: null,
  startDate: '',
  endDate: '',
  symbols: '',
})

export interface Series {
  key: string
  label: string
  points: Array<number | null>
}

export interface ChartData {
  labels: string[]
  symbols: string[]
  seriesMap: Record<string, Array<number | null>>
}

export function useDashboardState() {
  return { filters, datasets, records }
}
