<script setup lang="ts">
import { computed } from 'vue'
import type { Dataset } from '@/types/dataset'
import type { DashboardFilters } from '@/composables/dashboard-composable'

const props = defineProps<{
  filters: DashboardFilters
  datasets: Dataset[]
}>()

const emit = defineEmits<{ 'update:filters': [DashboardFilters] }>()

const selectedDatasetId = computed({
  get: () => props.filters.selectedDatasetId,
  set: (value: string | null) => {
    const dataset = props.datasets.find((ds) => ds.datasetId === value)
    emit('update:filters', {
      ...props.filters,
      selectedDatasetId: value,
      selectedDatasetName: dataset?.datasetName ?? '',
      selectedColumns: [],
    })
  },
})

const datasetOptions = computed(() =>
  props.datasets.map((ds) => ({
    label: `${ds.datasetName} (${ds.rowCount} rows)`,
    value: ds.datasetId,
  })),
)
</script>

<template>
  <div class="rounded-2xl bg-white p-5 shadow-sm">
    <div class="mb-3 flex items-center justify-between">
      <div>
        <h2 class="text-base font-semibold text-slate-900">Select Dataset</h2>
      </div>
    </div>

    <div class="mt-2">
      <label class="text-xs font-medium text-slate-700">Dataset</label>
      <n-select
        v-model:value="selectedDatasetId"
        :options="datasetOptions"
        placeholder="Select"
        filterable
        clearable
        class="mt-1 w-full"
      />
    </div>
  </div>
</template>
