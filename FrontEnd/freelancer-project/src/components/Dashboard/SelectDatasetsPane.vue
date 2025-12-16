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

const datasetOptions = computed(() =>
  props.datasets.map(ds => ({
    label: `${ds.datasetName} (${ds.rowCount} rows)`,
    value: ds.datasetName,
  }))
)

</script>

<template>
  <div class="rounded-2xl bg-white p-5 shadow-sm">
    <div class="mb-3 flex items-center justify-between">
      <div>
        <h2 class="text-base font-semibold text-slate-900">
          Select (Dataset)
        </h2>
      </div>
    </div>

    <div class="mt-2">
      <label class="text-xs font-medium text-slate-700">Dataset</label>
      <n-select
        v-model:value="selectedDatasetName"
        :options="datasetOptions"
        placeholder="Select"
        filterable
        clearable
        class="mt-1 w-full"/>
    </div>
  </div>
</template>