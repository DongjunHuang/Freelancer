<script setup lang="ts">
import { ref } from 'vue'
import { DashboardEvents } from '@/constants/events'
import { DATE_PRESETS, timePresets } from '@/constants/datePresets'

import type { DashboardFilters } from '@/composables/DashboardState'
import type { ColumnMeta } from '@/api/types'

// Only read
const props = defineProps<{filters: DashboardFilters, metricColumns: ColumnMeta[]}>()
const emit = defineEmits<{'update:filters': [DashboardFilters], 'generate': []}>()

// Preset time
// ========================================
const activePreset = ref<string | null>(null)

function applyPreset(value: string) {
  activePreset.value = value

  const today = new Date()
  const end = today.toISOString().slice(0, 10)

  const start = new Date(today)
  const days = DATE_PRESETS[value] ?? 0
  start.setDate(start.getDate() - days)

  emit(DashboardEvents.UpdateFilters, {
    ...props.filters,
    startDate: start.toISOString().slice(0, 10),
    endDate: end,
  })
}

function updateStartDate(e: Event) {
  const value = (e.target as HTMLInputElement).value
  emit(DashboardEvents.UpdateFilters, {
    ...props.filters,
    startDate: value,
  })
}

function updateEndDate(e: Event) {
  const value = (e.target as HTMLInputElement).value
  emit(DashboardEvents.UpdateFilters, {
    ...props.filters,
    endDate: value,
  })
}

// Events for button click
// ========================================
// If the generate graph button can be loaded
const MAX_SELECTED = 2

function onToggleColumn(name: string, event: Event) {
  const checked = (event.target as HTMLInputElement).checked
  const current = props.filters.selectedColumns

  const next = checked
    ? [...current, name]                       
    : current.filter((col) => col !== name) 

  emit(DashboardEvents.UpdateFilters, {
    ...props.filters,
    selectedColumns: next,
  })
}

function isColumnDisabled(name: string) {
  const current = props.filters.selectedColumns
  if (current.includes(name)) 
    return false
  return current.length >= MAX_SELECTED
}

function updateSymbolString(event: Event) {
  const value = (event.target as HTMLInputElement).value

  emit(DashboardEvents.UpdateFilters, {
    ...props.filters,
    symbols: value
  })
}

</script>

<template>
  <div class="rounded-2xl bg-white p-5 shadow-sm">
    <div class="mb-3">
      <h2 class="text-base font-semibold text-slate-900">
        Select columns and data range
      </h2>
      <p class="mt-1 text-xs text-slate-500">
        select at most 2 columns，then select time range
      </p>
    </div>

    <!-- 🔹 Symbols -->
    <div class="mb-4">
      <label class="text-xs font-medium text-slate-700">
        Symbols
      </label>
      <input
        :value="filters.symbols"
        @input="updateSymbolString($event)"
        type="text"
        class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white"
        placeholder="e.g. AAPL, MSFT, SPX"
      />
      <p class="mt-1 text-[11px] text-slate-400">
        Use comma or space to separate multiple symbols. 
      </p>
    </div>

    <!-- Columns selection -->
    <div class="grid gap-4 md:grid-cols-2">
      <div>
        <label class="text-xs font-medium text-slate-700">
          Columns（at most 2）
        </label>
        <div class="mt-1 max-h-40 space-y-1 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50 p-2">
          <label
            v-for="col in metricColumns"
            :key="col.columnName"
            class="flex cursor-pointer items-center justify-between rounded-lg px-2 py-1 text-xs hover:bg-slate-100">
            <div class="flex items-center gap-2">
              <input
                type="checkbox"
                :value="col.columnName"
                :checked="filters.selectedColumns.includes(col.columnName)"
                @change="onToggleColumn(col.columnName, $event)"
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

          <p v-if="metricColumns.length === 0"
            class="py-2 text-center text-xs text-slate-400">
            Current dataset does not have Metric columns
          </p>
        </div>
        <p class="mt-1 text-[11px] text-slate-400">
          Selected {{ filters.selectedColumns?.length ?? 0}}/2
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
              :value="filters.startDate"
              @input="updateStartDate($event)"
              type="date"
              class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-2 py-1.5 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white"
            />
          </div>
          <div>
            <span class="block text-[11px] text-slate-500">End date</span>
            <input
              :value="filters.endDate"
              @input="updateEndDate($event)"
              type="date"
              class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-2 py-1.5 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>