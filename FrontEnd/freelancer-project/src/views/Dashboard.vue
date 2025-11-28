<script setup lang="ts">
// Import necessarry dependencies
import { computed, ref, onMounted, watch} from 'vue'
import { fetchDatasets, fetchDatapoints} from '@/api/dashboard'  
import { useDashboardState } from '@/composables/DashboardState'

// Imports components
import SelectDatasetsPane from '@/components/Dashboard/SelectDatasetsPane.vue'
import SelectPropsPane from '@/components/Dashboard/SelectPropsPane.vue'
import SingleMetricChart from '@/components/Dashboard/SingleMetricChart.vue'
import DisplayDetailsPane from '@/components/Dashboard/DisplayDetailsPane.vue'
import RightSidePane from '@/components/Dashboard/RightSidePane.vue'

// Import types
import type { ColumnMeta, Series } from '@/api/types'
const { filters, datasets, records } = useDashboardState()

const error = ref('')
const loading = ref(false)

const selectedDataset = computed(() => datasets.value.find((d) => d.datasetName === filters.selectedDatasetName) || null)

// Mounted to call, load the datasets metadata from the client
async function loadData() {
  try {
    loading.value = true
    datasets.value = await fetchDatasets()
  } catch (e) {
    console.error(e)
    error.value = 'Failed to load data'
  } finally {
    loading.value = false
  }
}

// Fetch the datapoints from the backend
async function generate() {
  if (!filters.selectedDatasetName) 
    return

  try {
    loading.value = true
    error.value = ''

    const res = await fetchDatapoints({
      datasetName: filters.selectedDatasetName,
      startDate: filters.startDate,
      endDate: filters.endDate,
      columns: filters.selectedColumns,
      symbols: filters.symbols
    })

    records.value = res.data 
    console.log("[Records] ", records)
  } catch (e) {
    console.error(e)
    error.value = 'Failed to load records'
  } finally {
    loading.value = false
  }
}

// Available metric columns
const availableMetricColumns = computed<ColumnMeta[]>(() => {
  return selectedDataset.value
    ? selectedDataset.value.headers.filter(h => h.metric)
    : []
})

function buildAllSeriesForColumn(column: string): Series[] {
  if (!displayRecord.value) return []

  const { datapoints } = displayRecord.value
  const colKey = column.toUpperCase()   // 如果后端用大写存 key 的话

  return Object.entries(datapoints).map(([symbol, points]) => {
    const ys = points.map((p, idx) => {
      const raw = p.values[colKey]

      console.log(
        `[series=${symbol}] point[${idx}]`,
        'raw =', raw,
        'typeof =', typeof raw
      )

      if (raw == null || raw === '') {
        // 用 null 表示“这里没值”，Chart 会自动断开
        return null
      }

      const n = typeof raw === 'number' ? raw : Number(raw)

      if (!Number.isFinite(n)) {
        console.log(
          `[series=${symbol}] point[${idx}] -> invalid number, set to null`
        )
        return null
      }

      console.log(
        `[series=${symbol}] point[${idx}] -> parsed number:`,
        n
      )
      return n
    })

    console.log(`[series=${symbol}] final ys =`, ys)

    return {
      key: symbol,
      label: symbol,
      points: ys,
    }
  })
}

const labels = computed(() => {
  const firstSymbol = Object.keys(displayRecord.value!.datapoints)[0]
  return displayRecord.value!.datapoints[firstSymbol].map(p => p.recordDate)
})

watch(
  () => filters.selectedDatasetName,
  (val) => {
    console.log('[selectedDatasetName]', val)
  }
)
watch(
  () => availableMetricColumns.value,
  (cols) => {
    console.log('[metricColumns]', cols)
  },
  { deep: true }
)
watch(
  () => selectedDataset.value,
  (ds) => {
    console.log('[selectedDataset.headers]', ds?.headers)
  },
  { deep: true }
)

const canLoad = computed(
  () =>
    !!filters.selectedDatasetName &&
    filters.selectedColumns.length > 0 &&
    !!filters.startDate &&
    !!filters.endDate,
)

const displayRecord = computed(() => {
  return records.value
})


// Load data when mounted
onMounted(loadData)
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <main class="mx-auto flex w-5/6 max-w-7xl items-start gap-6 py-6">
      <section class="flex-1 space-y-4">
        <div>
          <SelectDatasetsPane
            :filters="filters"
            :datasets="datasets"
            @update:filters="next => Object.assign(filters, next)"
          />
        </div>

        <!-- Column & Time selection -->
        <div>
          <SelectPropsPane
            :filters="filters"
            :metricColumns="availableMetricColumns"
            @update:filters="(next) => Object.assign(filters, next)"
          />
        </div>

        <!-- graph show -->
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <h2 class="text-base font-semibold text-slate-900">
              Data diagram
            </h2>
            <span class="text-[11px] text-slate-400">
              {{ records ? Object.values(records.datapoints ?? {}).flat().length : 0 }} records
            </span>
          </div>

          <p v-if="!displayRecord" class="text-xs text-slate-500">
            Please select dataset、date range、columns，then click Generate graph.
          </p>

          <div
            v-else
            class="flex flex-col gap-4">
            <SingleMetricChart
              v-for="col in filters.selectedColumns"
              :key="col"
              :labels="labels"
              :all-series="buildAllSeriesForColumn(col)"
              :column="col"
            />
          </div>
        </div>

        <!-- Simply showcase -->
        <div>
          <DisplayDetailsPane 
            :filters="filters" />
        </div>
      </section>

      <aside class="hidden w-72 flex-none space-y-4 lg:block">
        <RightSidePane :filters="filters" />
        
        <!-- Generate graph button -->
        <div>
          <button
            type="button"
            @click="generate" :disabled="!canLoad"
            class="rounded-full px-4 py-2 text-xs font-medium"
            :class="
              canLoad
                ? 'bg-slate-900 text-white hover:bg-slate-800'
                : 'cursor-not-allowed bg-slate-200 text-slate-400'">
            Generate graph
          </button>
        </div>
      </aside>
    </main>
  </div>
</template>