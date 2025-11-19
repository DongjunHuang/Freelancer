<template>
  <div class="min-h-screen bg-slate-50">
    <main class="mx-auto flex max-w-6xl gap-6 px-6 py-6">
      <!-- Left side panel -->
      <section class="flex-1 space-y-4">
        <!-- Dataset select -->
        <div class="rounded-2xl bg-white p-5 shadow-sm">
          <div class="mb-3 flex items-center justify-between">
            <div>
              <h2 class="text-base font-semibold text-slate-900">
                Select (Dataset)
              </h2>
              <p class="mt-1 text-xs text-slate-500">
                Select your dataset first，then select columns and data range
              </p>
            </div>
            <button
              type="button"
              class="rounded-full bg-slate-900 px-4 py-2 text-xs font-medium text-white hover:bg-slate-800"
            >
              Manage datasets
            </button>
          </div>

          <div class="mt-2">
            <label class="text-xs font-medium text-slate-700">Dataset</label>
            <select
              v-model="selectedDatasetName"
              class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-800 outline-none focus:border-slate-400 focus:bg-white">
              <option disabled value="">Select</option>
              <option
                v-for="ds in datasets"
                :key="ds.datasetName"
                :value="ds.datasetName"> {{ ds.datasetName }}（{{ ds.rowCount }} rows）
              </option>
            </select>
          </div>
        </div>

        <!-- Column & Time selection -->
        <div class="rounded-2xl bg-white p-5 shadow-sm">
          <div class="mb-3">
            <h2 class="text-base font-semibold text-slate-900">
              Select columns and data range
            </h2>
            <p class="mt-1 text-xs text-slate-500">
              select at most 2 columns，then select time range
            </p>
          </div>

          <!-- Columns selection -->
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="text-xs font-medium text-slate-700">
                Columns（at most 2）
              </label>
              <div
                class="mt-1 max-h-40 space-y-1 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50 p-2"
              >
                <label
                  v-for="col in availableMetricColumns"
                  :key="col.columnName"
                  class="flex cursor-pointer items-center justify-between rounded-lg px-2 py-1 text-xs hover:bg-slate-100">
                  <div class="flex items-center gap-2">
                    <input
                      type="checkbox"
                      :value="col.columnName"
                      v-model="selectedColumns"
                      :disabled="isColumnDisabled(col.columnName)"
                      class="h-3 w-3 rounded border-slate-300 text-slate-900 focus:ring-slate-500"
                    />
                    <span class="font-medium text-slate-800">
                      {{ col.columnName }}
                    </span>
                  </div>
                  <span class="text-[10px] uppercase text-slate-400">
                    {{ col.dataType }}
                  </span>
                </label>

                <p
                  v-if="availableMetricColumns.length === 0"
                  class="py-2 text-center text-xs text-slate-400">
                  Current dataset does not have columns
                </p>
              </div>
              <p class="mt-1 text-[11px] text-slate-400">
                Selected {{ selectedColumns.length }}/2
              </p>
            </div>

            <!-- Time range -->
            <div>
              <label class="text-xs font-medium text-slate-700">
                Date range
              </label>

              <div class="mt-1 flex flex-wrap gap-2">
                <button
                  v-for="preset in timePresets"
                  :key="preset.value"
                  type="button"
                  @click="applyPreset(preset.value)"
                  class="rounded-full border px-3 py-1 text-xs"
                  :class="
                    preset.value === activePreset
                      ? 'border-slate-900 bg-slate-900 text-white'
                      : 'border-slate-200 bg-slate-50 text-slate-700 hover:border-slate-400'
                  "
                >
                  {{ preset.label }}
                </button>
              </div>

              <!-- Define Date -->
              <div class="mt-3 grid grid-cols-2 gap-2">
                <div>
                  <span class="block text-[11px] text-slate-500">Start date</span>
                  <input
                    v-model="dateFrom"
                    type="date"
                    class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-2 py-1.5 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white"
                  />
                </div>
                <div>
                  <span class="block text-[11px] text-slate-500">End date</span>
                  <input
                    v-model="dateTo"
                    type="date"
                    class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-2 py-1.5 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Generate graph button -->
          <div class="mt-4 flex items-center justify-between">
            <p class="text-[11px] text-slate-400">
              At lease select one dateset, 1 column and 1 valid date range
            </p>
            <button
              type="button"
              @click="loadData"
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
        </div>

        <!-- graph show -->
        <div class="rounded-2xl bg-white p-5 shadow-sm">
          <div class="mb-3 flex items-center justify-between">
            <h2 class="text-base font-semibold text-slate-900">
              Data diagram
            </h2>
            <span class="text-[11px] text-slate-400">
              {{ chartTitle }}
            </span>
          </div>

          <div
            class="flex h-64 items-center justify-center rounded-2xl border border-dashed border-slate-200 bg-slate-50"
          >
            <p v-if="chartData.length === 0" class="text-xs text-slate-400">
              Pelase select data set, columns and date range, then click "Generate graph"
            </p>
            <p v-else class="text-xs text-slate-500">
              这里接入你的图表组件 (ECharts / Chart.js)，current data points：{{ chartData.length }}
            </p>
          </div>

          <!-- Simply showcase -->
          <div class="mt-4">
            <div class="mb-2 flex items-center justify-between">
              <h3 class="text-sm font-semibold text-slate-900">
                Details
              </h3>
              <button
                type="button"
                class="text-xs text-slate-500 hover:text-slate-800"
              >
                Show all
              </button>
            </div>
            <div class="overflow-hidden rounded-2xl border border-slate-100">
              <table class="min-w-full text-left text-xs">
                <thead class="bg-slate-50 text-slate-500">
                  <tr>
                    <th class="px-4 py-2">时间</th>
                    <th
                      v-for="colKey in selectedColumns"
                      :key="colKey"
                      class="px-4 py-2"
                    >
                      {{ columnLabel(colKey) }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row in chartData.slice(0, 10)"
                    :key="row.time + JSON.stringify(row)"
                    class="border-t border-slate-50"
                  >
                    <td class="px-4 py-2 text-slate-500">
                      {{ row.time }}
                    </td>
                    <td
                      v-for="colKey in selectedColumns"
                      :key="colKey"
                      class="px-4 py-2 text-slate-800"
                    >
                      {{ row[colKey] }}
                    </td>
                  </tr>
                  <tr v-if="chartData.length === 0">
                    <td
                      colspan="3"
                      class="px-4 py-6 text-center text-slate-400"
                    >
                      Data not available
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>

      <!-- Right side -->
      <aside class="hidden w-72 space-y-4 lg:block">
        <div class="rounded-2xl bg-white p-4 shadow-sm">
          <h3 class="text-sm font-semibold text-slate-900">
            Current dataset
          </h3>
          <p class="mt-1 text-xs text-slate-500">
            After selecting dataset，should display some basic info here。
          </p>
          <div class="mt-3 rounded-xl bg-slate-50 p-3 text-xs text-slate-600">
            <p>
              Name：
              <span class="font-medium">
                {{ currentDataset?.name || 'Not selected' }}
              </span>
            </p>
            <p class="mt-1">
              Column num：
              <span class="font-medium">
                {{ currentDataset?.columns.length || '-' }}
              </span>
            </p>
            <p class="mt-1 text-[11px] text-slate-400">
              这里以后可以展示：记录数、时间范围、上次更新等。
            </p>
          </div>
        </div>

        <div class="rounded-2xl bg-white p-4 shadow-sm">
          <h3 class="text-sm font-semibold text-slate-900">
            常用视图
          </h3>
          <p class="mt-1 text-xs text-slate-500">
            未来可以在这里保存常用的 dataset + 字段 + 时间组合。
          </p>
          <button
            type="button"
            class="mt-3 w-full rounded-full border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-700 hover:border-slate-400"
          >
            + 保存当前视图
          </button>
        </div>

        <div class="rounded-2xl bg-white p-4 shadow-sm">
          <h3 class="text-sm font-semibold text-slate-900">
            使用提示
          </h3>
          <ul class="mt-2 space-y-1 text-xs text-slate-500">
            <li>· 先选 dataset，再选列和时间。</li>
            <li>· 数值列最多选择 2 个，避免图表过于拥挤。</li>
            <li>· 时间范围过长时建议按月聚合。</li>
          </ul>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted} from 'vue'
import type { Dataset, ColumnMeta, ColumnType } from '@/api/types'
import { fetchDatasets } from '@/api/dashboard'  

const datasets = ref<Dataset[]>([])
const selectedDatasetName = ref('')
const selectedColumns = ref<string[]>([])
const selectedDatasetId = ref<string>('')

const loading = ref(false)
const error = ref('')

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    loading.value = true

    const res = await fetchDatasets()
    datasets.value = res.data

    console.log('Datasets:', datasets.value)
  } catch (e) {
    console.error(e)
    error.value = 'Failed to load data'
  } finally {
    loading.value = false
  }
}

const selectedDataset = computed<Dataset | null>(() => {
  return datasets.value.find(d => d.datasetName === selectedDatasetName.value) ?? null
})

const availableMetricColumns = computed<ColumnMeta[]>(() => {
  if (!selectedDataset.value)
    return []

  return selectedDataset.value.headers.filter(
    (c) => c.isMetric
  )
})


const dateFrom = ref<string>('')
const dateTo = ref<string>('')

// 图表数据占位
const chartData = ref<Array<Record<string, any>>>([])

// 时间 preset
const timePresets = [
  { label: 'last 7 days', value: '7d' },
  { label: 'last 30 days', value: '30d' },
  { label: 'last 90 days', value: '90d' },
]
const activePreset = ref<string | null>(null)

const currentDataset = computed(() =>
  datasets.value.find((d) => d.id === selectedDatasetId.value),
)

const columnLabel = (key: string) =>
  currentDataset.value?.columns.find((c) => c.key === key)?.label ?? key

const canLoad = computed(
  () =>
    !!selectedDatasetId.value &&
    selectedColumns.value.length > 0 &&
    !!dateFrom.value &&
    !!dateTo.value,
)

const chartTitle = computed(() => {
  if (!currentDataset.value || selectedColumns.value.length === 0) return 'not select'
  return ''
  //return `${currentDataset.value.name} · ${selectedColumns.map((k) => columnLabel(k)).join(' / ')}`
})

function isColumnDisabled(key: string) {
  return selectedColumns.value.length >= 2 && !selectedColumns.value.includes(key)
}

function applyPreset(value: string) {
  activePreset.value = value

  const today = new Date()
  const end = today.toISOString().slice(0, 10)
  const startDate = new Date(today)

  if (value === '7d') startDate.setDate(startDate.getDate() - 6)
  if (value === '30d') startDate.setDate(startDate.getDate() - 29)
  if (value === '90d') startDate.setDate(startDate.getDate() - 89)

  dateFrom.value = startDate.toISOString().slice(0, 10)
  dateTo.value = end
}
</script>