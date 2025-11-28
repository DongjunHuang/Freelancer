<script setup lang="ts">
import { computed } from 'vue'
import type { Dataset } from '@/api/types'
import type { DashboardFilters } from '@/composables/DashboardState'

const props = defineProps<{
  filters: DashboardFilters
  datasets: Dataset[]
}>()

const emit = defineEmits<{ 'update:filters': [DashboardFilters] }>()

const selectedDatasetName = computed({
  get: () => props.filters.selectedDatasetName,
  set: (value: string) => {
    emit('update:filters', {
      ...props.filters,
      selectedDatasetName: value,
      selectedColumns: [],
    })
  },
})
</script>

<template>
  <div class="rounded-2xl bg-white p-5 shadow-sm">
    <div class="mb-3 flex items-center justify-between">
      <div>
        <h2 class="text-base font-semibold text-slate-900">
          Select (Dataset)
        </h2>
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
          :value="ds.datasetName"> 
            {{ ds.datasetName }}（{{ ds.rowCount }} rows）
        </option>
      </select>
    </div>
  </div>
</template>