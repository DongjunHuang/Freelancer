<script setup lang="ts">
const props = defineProps<{
  labels: string[]
  column: string
  symbols: string[]
  selectedKeys: string[]
  seriesMap: Record<string, Array<number | null>>
}>()
import { ref, watch, onMounted, onBeforeUnmount,computed, nextTick} from 'vue'
import * as echarts from 'echarts'

const onResize = () => chart?.resize()
const chartEl = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

function buildOption(): echarts.EChartsOption {
  const series = props.selectedKeys.map(sym => ({
    name: sym,
    type: 'line' as const,
    showSymbol: false,
    smooth: false,
    data: props.labels.map((d, i) => [new Date(d), props.seriesMap[sym]?.[i] ?? null]),
  }))

  return {
    animation: false,
    grid: { left: 44, right: 16, top: 20, bottom: 45, containLabel: true },

    legend: { show: false },

    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' }
    },

    xAxis: {
      type: 'time',
      axisLabel: { hideOverlap: true }
    },

    yAxis: {
      type: 'value',
      name: props.column,
      nameGap: 18
    },

    dataZoom: [
      { type: 'inside', xAxisIndex: 0, zoomOnMouseWheel: true, moveOnMouseMove: true },
      { type: 'slider', xAxisIndex: 0, height: 18, bottom: 12 }
    ],

    series
  }
}

function render() {
  if (!chartEl.value) {
    return
  }

  const rect = chartEl.value.getBoundingClientRect()
  console.log('[chart] rect', rect.width, rect.height)
  console.log('[chart] labels', props.labels.length)
  console.log('[chart] selectedKeys', props.selectedKeys)
  console.log('[chart] seriesMap keys', Object.keys(props.seriesMap || {}))

  if (rect.width === 0 || rect.height === 0) {
    console.warn('[chart] container has 0 size, skip render')
    return
  }

  if (!chart) chart = echarts.init(chartEl.value)
  const option = buildOption()
  console.log('[chart] option series len', (option as any).series?.length)
  chart.setOption(option, true)
}

onMounted(() => {
  render()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})

watch(
  () => [props.labels, props.selectedKeys, props.seriesMap, props.column],
  () => render(),
  { deep: true, immediate: true }
)

const emit = defineEmits<{
  (e: 'update:selectedKeys', v: string[]): void
}>()

const selectedKeysModel = computed<string[]>({
  get: () => props.selectedKeys ?? [],
  set: (v) => emit('update:selectedKeys', v)
})

function selectAll() {
  emit('update:selectedKeys', props.symbols.slice())
}

function unselectAll() {
  emit('update:selectedKeys', [])
}
</script>

<template>
  <div class="rounded-2xl bg-white p-4 shadow-sm">
    <!-- Header -->
    <div class="mb-3 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <h3 class="text-sm font-semibold text-slate-900">
          {{ column }}
        </h3>
        <span class="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-500">
          {{ selectedKeys.length }}/{{ symbols.length }}
        </span>
      </div>
    </div>

    <div class="mt-1 flex flex-col gap-4 md:flex-row">
      <!-- left: diagram -->
      <div class="flex-1">
        <div
          class="h-[360px] md:h-[480px] rounded-xl border border-dashed border-slate-200 bg-slate-50 p-3">
          
          <p v-if="!labels.length" class="text-xs text-slate-500">
            No data. Please generate records first.
          </p>

          <!-- ECharts container -->
          <div v-else ref="chartEl" class="h-full w-full" />
        </div>
      </div>

      <!-- right: symbol selection -->
      <div class="w-full md:w-48">
        <div class="rounded-xl border border-slate-200 bg-slate-50 p-2">
          <div class="mb-1 flex items-center justify-between">
            <span class="text-[11px] font-medium text-slate-700">
              Symbols
            </span>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="text-[10px] text-slate-500 hover:text-slate-900"
                @click="selectAll">
                All
              </button>

              <button
                type="button"
                class="text-[10px] text-slate-500 hover:text-slate-900"
                @click="unselectAll">
                None
              </button>

              <span class="text-[10px] text-slate-400">
                {{ symbols.length }}
              </span>  
            </div>
          </div>

          <p v-if="!symbols.length" class="py-2 text-[11px] text-slate-400">
            No symbols available.
          </p>

          <div v-else class="max-h-40 space-y-1 overflow-y-auto pr-1">
            <label
              v-for="sym in symbols"
              :key="sym"
              class="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1 text-xs hover:bg-slate-100">
              <input
                type="checkbox"
                class="h-3 w-3 rounded border-slate-300 text-slate-900 focus:ring-slate-500"
                :value="sym"
                v-model="selectedKeysModel"/>
              <span class="truncate text-slate-800" :title="sym">
                {{ sym }}
              </span>
            </label>
          </div>

          <p class="mt-1 text-[10px] text-slate-400">
            Selected {{ selectedKeys.length }}/{{ symbols.length }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>