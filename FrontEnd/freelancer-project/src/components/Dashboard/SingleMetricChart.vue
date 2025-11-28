<script setup lang="ts">
import { ref, watch, onUnmounted, onMounted, onBeforeUnmount, computed } from 'vue'
import type { Series, DataPoint } from '@/api/types'
import { Chart, LineController, LineElement, PointElement, LinearScale, CategoryScale, Tooltip, Legend } from 'chart.js'
Chart.register(LineController, LineElement, PointElement, LinearScale, CategoryScale, Tooltip, Legend)

const props = defineProps<{
  column: string
  labels: string[]
  // like
  /* [
  { key: "AAPL", label: "Apple",  points: [150, 153, 158, 160] },
  { key: "MSFT", label: "Microsoft", points: [300, 302, 298, 310] },
  { key: "TSLA", label: "Tesla", points: [250, 240, 245, 255] }
  ]
  */ 
  allSeries: Series[]
}>()

const symbols = computed(() => props.allSeries.map(s => s.key))
const selectedKeys = ref<string[]>([])
const chartCanvas = ref<HTMLCanvasElement | null>(null)
let chart: Chart<'line'> | null = null

function buildChartData() {
  console.log("⚡ buildChartData() called")
  console.log("props.labels =", props.labels)
  console.log("selectedKeys =", selectedKeys.value)
  console.log("allSeries =", props.allSeries)

  const active = props.allSeries.filter(series =>
    selectedKeys.value.includes(series.key)
  )

  console.log("active series =", active)

  return {
    labels: props.labels,
    datasets: active.map(series => {
      console.log(`\n📌 Processing series:`, series.label)

      const convertedPoints = series.points.map((v, idx) => {
        console.log(`  raw[${idx}] =`, v, "typeof =", typeof v)

        if (v == null) {
          console.log(`  → [${idx}] = null (kept as null)`)
          return null
        }

        const num = typeof v === "number" ? v : Number(v)

        if (!Number.isFinite(num)) {
          console.log(`  → [${idx}] = null (invalid number)`)
          return null
        }

        console.log(`  → [${idx}] converted to number:`, num)
        return num
      })

      console.log("convertedPoints =", convertedPoints)

      return {
        label: series.label,
        data: convertedPoints,
        tension: 0.2,
        pointRadius: 0,
      }
    }),
  }
}

function destroyChart() {
  if (chart) {
    chart.destroy()
    chart = null
  }
}

function redraw() {
  if (!chartCanvas.value) return

  const data = buildChartData()

  if (!props.labels.length || !data.datasets.length) {
    destroyChart()
    return
  }

  destroyChart()

  const ctx = chartCanvas.value.getContext('2d')
  if (!ctx) return

  chart = new Chart(ctx, {
    type: 'line',
    data,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      scales: {
        x: { title: { display: true, text: 'Date' } },
        y: { title: { display: true, text: props.column } },
      },
      plugins: {
        legend: { display: true },
        tooltip: { enabled: true },
      },
    },
  })
}
onMounted(() => {
  selectedKeys.value = symbols.value.slice()
  redraw()
})

onUnmounted(() => {
  if (chart) {
    chart.destroy()
    chart = null
  }
})
watch(
  [() => props.allSeries, () => props.labels, selectedKeys],
  () => redraw(),
  { deep: true }
)

onBeforeUnmount(() => {
  destroyChart()
})
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
          {{ selectedKeys.length }}/{{ symbols.length }} series
        </span>
      </div>
    </div>

    <div class="mt-1 flex flex-col gap-4 md:flex-row">
      <!-- 左侧：图表 -->
      <div class="flex-1">
        <div
          class="h-52 rounded-xl border border-dashed border-slate-200 bg-slate-50 p-3 flex items-center justify-center"
        >
          <p v-if="!labels.length" class="text-xs text-slate-500">
            No data. Please generate records first.
          </p>
          <div v-else class="h-full w-full">
            <canvas ref="chartCanvas" class="h-full w-full" />
          </div>
        </div>
      </div>

      <!-- 右侧：Symbol 选择 -->
      <div class="w-full md:w-48">
        <div class="rounded-xl border border-slate-200 bg-slate-50 p-2">
          <div class="mb-1 flex items-center justify-between">
            <span class="text-[11px] font-medium text-slate-700">
              Symbols / Indexes
            </span>
            <span class="text-[10px] text-slate-400">
              {{ symbols.length }}
            </span>
          </div>

          <p
            v-if="!symbols.length"
            class="py-2 text-[11px] text-slate-400"
          >
            No symbols available.
          </p>

          <div
            v-else
            class="max-h-40 space-y-1 overflow-y-auto pr-1"
          >
            <label
              v-for="sym in symbols"
              :key="sym"
              class="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1 text-xs hover:bg-slate-100"
            >
              <input
                type="checkbox"
                class="h-3 w-3 rounded border-slate-300 text-slate-900 focus:ring-slate-500"
                :value="sym"
                v-model="selectedKeys"
              />
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
