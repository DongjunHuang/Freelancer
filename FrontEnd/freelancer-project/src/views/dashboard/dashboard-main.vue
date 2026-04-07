<script setup lang="ts">
import axios from 'axios'
import { computed, ref, onMounted, watch } from 'vue'
import { fetchDatasets, fetchDatapoints } from '@/api/dashboard'
import { useDashboardState } from '@/composables/dashboard-state'

// Imports components
import SelectDatasetsPane from '@/components/dashboard/select-datasets-pane.vue'
import SelectPropsPane from '@/components/dashboard/select-props-pane.vue'
import SingleMetricChart from '@/components/dashboard/single-metric-chart.vue'
import DisplayDetailsPane from '@/components/dashboard/display-details-pane.vue'
import RightSidePane from '@/components/dashboard/right-side-pane.vue'

// Import types
import type { ColumnMeta, FetchRecordsResp, DataPoint, ChartData } from '@/types/user'

const { filters, datasets } = useDashboardState()
const error = ref('')
const loading = ref(false)
const resp = ref<FetchRecordsResp | null>(null)

const selectedDataset = computed(
  () => datasets.value.find((d) => d.datasetName === filters.selectedDatasetName) || null,
)
const uiState = computed(() => {
  if (loading.value) return 'loading'
  if (error.value) return 'error'
  if (resp.value) return 'success'
  return 'empty'
})
// Load data when mounted
onMounted(loadData)

const selectedKeysByCol = ref<Record<string, string[]>>({})

function buildLabelsForColumn(points: DataPoint[], col: string): string[] {
  const set = new Set<string>()

  for (const p of points) {
    if (p.column === col) {
      set.add(p.recordDate)
    }
  }
  return Array.from(set).sort((a, b) => +new Date(a) - +new Date(b))
}

function buildSymbolsForColumn(points: DataPoint[], col: string): string[] {
  const set = new Set<string>()
  for (const p of points) {
    if (p.column === col) {
      set.add(p.symbol)
    }
  }
  return Array.from(set).sort()
}

function buildSeriesMapForColumn(points: DataPoint[], col: string, labels: string[]) {
  const bySym = new Map<string, Map<string, number | null>>()

  for (const p of points) {
    if (p.column !== col) {
      continue
    }
    if (!bySym.has(p.symbol)) {
      bySym.set(p.symbol, new Map())
    }
    bySym.get(p.symbol)!.set(p.recordDate, p.value ?? null)
  }

  const out: Record<string, Array<number>> = {}
  for (const [sym, dateMap] of bySym.entries()) {
    out[sym] = labels.map((d) => dateMap.get(d) ?? 0)
  }
  return out
}

const chartsByCol = computed<Record<string, ChartData>>(() => {
  if (!resp.value) return {}

  const points = resp.value.datapoints
  const out: Record<string, ChartData> = {}

  for (const col of resp.value.columns) {
    const labels = buildLabelsForColumn(points, col)
    const symbols = buildSymbolsForColumn(points, col)
    const seriesMap = buildSeriesMapForColumn(points, col, labels)
    out[col] = { labels, symbols, seriesMap }
  }

  return out
})

watch(
  () => chartsByCol.value,
  (next) => {
    for (const [col, chart] of Object.entries(next)) {
      if (!selectedKeysByCol.value[col]) {
        selectedKeysByCol.value[col] = chart.symbols.slice(0, 5)
      }
    }
  },
  { immediate: true, deep: false },
)

// ================================
// Mounted to call, load the datasets metadata from the client
// Available metric columns
const availableMetricColumns = computed<ColumnMeta[]>(() => {
  return selectedDataset.value ? selectedDataset.value.headers.filter((h) => h.metric) : []
})

async function loadData() {
  try {
    loading.value = true
    const res = await fetchDatasets()
    datasets.value = res.data
  } catch (e) {
    console.error(e)
    error.value = 'Failed to load data'
  } finally {
    loading.value = false
  }
}

// Fetch the datapoints from the backend
async function generate() {
  loading.value = true
  error.value = ''
  resp.value = null
  if (!filters.selectedDatasetName) {
    console.log('Not able to find selectedDatasetName')
    return
  }

  try {
    loading.value = true
    error.value = ''

    const res = await fetchDatapoints(filters.selectedDatasetName, {
      startDate: filters.startDate,
      endDate: filters.endDate,
      columns: filters.selectedColumns,
      symbols: filters.symbols,
    })

    resp.value = res.data
  } catch (e: unknown) {
    if (axios.isAxiosError(e)) {
      error.value = e.response?.data?.message || 'Failed to load'
    } else {
      error.value = 'Failed to load'
    }
    console.log(error.value)
  } finally {
    loading.value = false
  }
}

const canLoad = computed(
  () =>
    !!filters.selectedDatasetName &&
    filters.selectedColumns.length > 0 &&
    !!filters.startDate &&
    !!filters.endDate,
)
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <main class="mx-auto flex w-5/6 max-w-7xl items-start gap-6 py-6">
      <section class="flex-1 space-y-4">
        <div>
          <SelectDatasetsPane
            :filters="filters"
            :datasets="datasets"
            @update:filters="(next) => Object.assign(filters, next)"
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
            <h2 class="text-base font-semibold text-slate-900">Data diagram</h2>
            <span class="text-[11px] text-slate-400">
              {{ resp ? Object.values(resp.datapoints ?? {}).flat().length : 0 }} records
            </span>
          </div>

          <div class="relative min-h-[300px]">
            <!-- loading -->
            <div
              v-if="uiState === 'loading'"
              class="absolute inset-0 z-10 flex items-center justify-center bg-white/70 rounded-2xl"
            >
              <NSpin size="large" />
            </div>

            <!-- error -->
            <div
              v-else-if="uiState === 'error'"
              class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
            >
              {{ error }}
            </div>

            <!-- success -->
            <div v-else-if="uiState === 'success'">
              <SingleMetricChart
                v-for="col in resp?.columns"
                :key="col"
                :labels="chartsByCol[col]?.labels ?? []"
                :column="col"
                :symbols="chartsByCol[col]?.symbols ?? []"
                :seriesMap="chartsByCol[col]?.seriesMap ?? {}"
                :selectedKeys="selectedKeysByCol[col] ?? []"
                @update:selectedKeys="(v) => (selectedKeysByCol[col] = v)"
              />
            </div>

            <!-- empty -->
            <p v-else class="text-xs text-slate-500">
              Please select dataset, date range, columns, then click Generate graph.
            </p>
          </div>
        </div>
        <!-- Simply showcase -->
        <div>
          <DisplayDetailsPane :filters="filters" />
        </div>
      </section>

      <aside class="hidden w-72 flex-none space-y-4 lg:block">
        <RightSidePane :filters="filters" />

        <!-- Generate graph button -->
        <div>
          <button
            type="button"
            @click="generate"
            :disabled="!canLoad"
            class="rounded-full px-4 py-2 text-xs font-medium"
            :class="
              canLoad
                ? 'bg-slate-900 text-white hover:bg-slate-800'
                : 'cursor-not-allowed bg-slate-200 text-slate-400'
            "
          >
            Generate graph
          </button>
        </div>
      </aside>
    </main>
  </div>
</template>
