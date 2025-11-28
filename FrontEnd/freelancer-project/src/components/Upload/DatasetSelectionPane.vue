<script setup lang="ts">
import {ref, computed} from 'vue'
import { UploadEvents } from '@/constants/events'
import type { UploadState } from '@/composables/UploadComposable'
import type { Dataset } from '@/api/types'

const props = defineProps<{ state: UploadState, datasets: Dataset[]}>()
const emit = defineEmits<{'update:state': [UploadState]}>()



function updateState(patch: Partial<UploadState>) {
  emit(UploadEvents.UpdateState, {
    ...props.state,
    ...patch
  })
}
const selectedMode = computed<'existing' | 'new'>({
  get: () => (props.state.isNewSelectedMode ? 'new' : 'existing'),

  set: (value) => {
    updateState({
      isNewSelectedMode: value === 'new'
    })
  },
})

const selectedDatasetName = computed({
  get: () => props.state.selectedDatasetName,
  set: (value: string) => {
    updateState({ selectedDatasetName: value })
  },
})
</script>
<template>
    <div class="rounded-2xl bg-white p-4 shadow-sm">
      <div class="mb-3">
        <h2 class="text-base font-semibold text-slate-900">
          Select dataset
        </h2>
        <p class="mt-1 text-xs text-slate-500">
          Choose an existing dataset or create a new one for this upload.
        </p>
      </div>
  
      <!-- 模式切换按钮 -->
      <div class="mb-3 flex gap-2">
        <button
          type="button"
          @click="selectedMode = 'existing'"
          class="rounded-full px-3 py-1 text-xs font-medium border"
          :class="
            selectedMode === 'existing'
              ? 'border-slate-900 bg-slate-900 text-white'
              : 'border-slate-200 bg-slate-50 text-slate-700 hover:border-slate-400'
          "
        >
          Use existing dataset
        </button>
  
        <button
          type="button"
          @click="selectedMode = 'new'"
          class="rounded-full px-3 py-1 text-xs font-medium border"
          :class="
            selectedMode === 'new'
              ? 'border-slate-900 bg-slate-900 text-white'
              : 'border-slate-200 bg-slate-50 text-slate-700 hover:border-slate-400'
          "
        >
          Create new dataset
        </button>
      </div>
  
      <div>
        <!-- existing dataset -->
        <div v-if="selectedMode === 'existing'">
          <label class="text-xs font-medium text-slate-700">
            Existing datasets
          </label>
          <select
            v-model="selectedDatasetName"
            class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-800 outline-none focus:border-slate-400 focus:bg-white"
          >
            <option disabled value="">Select</option>
            <option
              v-for="t in datasets"
              :key="t.datasetName"
              :value="t.datasetName"
            >
              {{ t.datasetName }} ({{ t.rowCount }} rows)
            </option>
          </select>
  
          <p
            v-if="!datasets.length"
            class="mt-1 text-[11px] text-orange-500"
          >
            No datasets found. Please create a new dataset first.
          </p>
        </div>
  
        <!-- new dataset -->
        <div v-else>
          <label class="text-xs font-medium text-slate-700">
            New dataset name
          </label>
          <input
            v-model.trim="selectedDatasetName"
            placeholder="e.g. prices_us_daily_2025"
            class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-800 outline-none focus:border-slate-400 focus:bg-white"
          />
          <p class="mt-1 text-[11px] text-slate-400">
            Use letters, numbers, and underscores. This will be used as the dataset identifier.
          </p>
        </div>
      </div>
    </div>
  </template>