<script setup lang="ts">
import type { DashboardFilters } from '@/composables/DashboardState'

const props = defineProps<{
  filters: DashboardFilters,
}>()
</script>

<template>
  <div class="rounded-2xl bg-white p-4 shadow-sm">
    <h3 class="text-sm font-semibold text-slate-900">
      Current dataset
    </h3>
    <div class="mt-3 rounded-xl bg-slate-50 p-3 text-xs text-slate-600">
      <p>
        Name：
        <span class="font-medium">
          {{ props.filters.selectedDatasetName || 'Not selected' }}
        </span>
      </p>
      <p class="mt-1">
        Total column num：
        <span class="font-medium">
          {{ props.filters.selectedColumns.length || '-' }}
        </span>
      </p>
      <p class="mt-1 text-[11px] text-slate-400">
        TODO: this could display: record time, date range, last update, etc
      </p>
    </div>
  </div>

  <div class="rounded-2xl bg-white p-4 shadow-sm">
    <h3 class="text-sm font-semibold text-slate-900">
      Normal view
    </h3>
    <p class="mt-1 text-xs text-slate-500">
      TODO: save normal combination of dataset + columns + time
    </p>
    <button
      type="button"
      class="mt-3 w-full rounded-full border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-700 hover:border-slate-400">
        + Save current view
    </button>
  </div>

  <div class="rounded-2xl bg-white p-4 shadow-sm">
    <h3 class="text-sm font-semibold text-slate-900">
      Tips
    </h3>
    <ul class="mt-2 space-y-1 text-xs text-slate-500">
      <li>First select dataset，then select columns and date。</li>
    </ul>
  </div>
</template>