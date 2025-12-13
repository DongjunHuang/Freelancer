<script setup lang="ts">
// Import 
import {ref, computed, watch} from 'vue'

// Import types
import type { UploadState, UploadStatePatch} from '@/composables/UploadComposable'
import type { Dataset } from '@/api/types'

// Consts
const props = defineProps<{ state: UploadState, datasets: Dataset[]}>()
const emit = defineEmits<{
  (e: 'update:state', next: UploadStatePatch): void
  (e: 'delete-dataset', name: string): void
}>()

const deleting = ref(false)

const datasetOptions = computed(() =>
  props.datasets.map(d => ({
    label: `${d.datasetName} (${d.rowCount} rows)`,
    value: d.datasetName
  }))
)
const selectedDataset = computed(() =>
  props.datasets.find(d => d.datasetName === selectedDatasetName.value) ?? null
)

const usingExisting = computed({
  get: () => !props.state.dataset.isNew,
  set: (val: boolean) => {
    emit('update:state', { dataset: { isNew: !val } })
  }
})

const selectedDatasetName = computed({
  get: () => props.state.dataset.selectedName,
  set: (val: string ) => {
    emit('update:state', { dataset: { selectedName: val } })
  }
})

const newDatasetName = computed({
  get: () => props.state.dataset.newName,
  set: (val: string) => {
    emit('update:state', { dataset: { newName: val } })
  }
})

function handleDeleteDataset() {
  const name = props.state.dataset.selectedName
  if (!name) return

  emit('delete-dataset', name)

  emit('update:state', {
    dataset: { selectedName: '' }
  })
}

watch(
  () => props.state,
  (val) => {
    console.log('[child] props.state changed:', JSON.parse(JSON.stringify(val)))
  },
  { deep: true }
)
</script>

<template>
  <section
    class="mb-6 rounded-2xl bg-white px-6 py-5 shadow-sm border border-slate-100">
    <div class="flex items-start justify-between gap-3 mb-4">
      <div>
        <h2 class="text-sm font-semibold text-slate-900">Select dataset</h2>
        <p class="mt-1 text-xs text-slate-500">
          Choose an existing dataset or create a new one for this upload.
        </p>
      </div>

      <!-- Delete button -->
      <button
        v-if="usingExisting && selectedDataset"
        type="button"
        class="inline-flex items-center rounded-full border border-red-100 bg-red-50 px-3 py-1 text-[11px] font-medium text-red-600 hover:bg-red-100 disabled:opacity-40 disabled:cursor-not-allowed"
        :disabled="deleting"
        @click="handleDeleteDataset">
        <svg
          class="mr-1 h-3 w-3"
          viewBox="0 0 20 20"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5">
          <path
            d="M4 6h12M9 9v5M11 9v5M6 6l1 9h6l1-9M8 4h4l-.5-1h-3L8 4z"
            stroke-linecap="round"
            stroke-linejoin="round"/>
        </svg>
        Delete "{{ selectedDataset.datasetName }}"
      </button>
    </div>

    <!-- Toggle: Use existing / Create new -->
    <div class="mb-4 inline-flex rounded-full bg-slate-50 p-1 text-xs">
      <button
        type="button"
        class="px-3 py-1 rounded-full"
        :class="usingExisting ? 'bg-white shadow-sm text-slate-900' : 'text-slate-500 hover:text-slate-800'"
        @click="usingExisting = true">
        Use existing dataset
      </button>

      <button
        type="button"
        class="px-3 py-1 rounded-full"
        :class="!usingExisting ? 'bg-white shadow-sm text-slate-900' : 'text-slate-500 hover:text-slate-800'"
        @click="usingExisting = false; selectedDatasetName = ''">
        Create new dataset
      </button>
    </div>

    <!-- Existing datasets dropdown -->
    <div v-if="usingExisting" class="mt-2">
      <label class="mb-1 block text-[11px] font-medium text-slate-500">
        Existing datasets
      </label>

      <n-select
        v-model:value="selectedDatasetName"
        :options="datasetOptions"
        placeholder="Select a dataset…"
        filterable
        clearable
        class="w-full"/>

      <p v-if="selectedDataset" class="mt-1 text-[11px] text-slate-400">
        Selected: {{ selectedDataset.datasetName }} · {{ selectedDataset.rowCount }} rows
      </p>
    </div>

    <!-- ✅ New dataset name input -->
    <div v-else class="mt-2">
      <label class="mb-1 block text-[11px] font-medium text-slate-500">
        New dataset name
      </label>
      <input
        v-model="newDatasetName"
        type="text"
        placeholder="Enter a name for the new dataset…"
        class="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-800 shadow-inner focus:outline-none focus:border-slate-400 focus:bg-white"/>
    </div>
  </section>
</template>
  