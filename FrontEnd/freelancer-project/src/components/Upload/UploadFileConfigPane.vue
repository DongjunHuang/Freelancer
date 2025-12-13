<script setup lang="ts">
import { computed } from 'vue';
import type { UploadState, UploadStatePatch } from '@/composables/UploadComposable'

const props = defineProps<{state: UploadState}>()
const emit = defineEmits<{
  (e: 'update:state', next: UploadStatePatch): void
}>()
const dateFormats = [
  'yyyy-MM-dd',
  'yyyy/MM/dd',
  'MM/dd/yyyy',
  'dd/MM/yyyy'
];

const recordDateColumn = computed({
  get: () => props.state.config.recordDateColumn,
  set: (value: string) => {
    emit('update:state', { config: { recordDateColumn: value } })
  },
})

const recordDateFormat = computed({
  get: () => props.state.config.recordDateFormat, 
  set: (value: string) => {
    emit('update:state', { config: { recordDateFormat: value } })
  },
})

const symbol = computed({
  get: () => props.state.config.symbol,
  set: (value: string) => {
    emit('update:state', { config: { symbol: value } }) 
  },
})
</script>

<template>
  <div class="mt-4 rounded-2xl bg-white p-4 shadow-sm">
    <h3 class="text-sm font-semibold text-slate-900">
      Date & Symbol settings
    </h3>
    <p class="mt-1 text-[11px] text-slate-500">
      Choose which columns represent date and symbol, and how to parse date values.
    </p>

    <!-- Only display when headers exit -->
    <div v-if="props.state.config.headers.length" class="mt-3 space-y-3">
      <!-- Date column -->
      <div>
        <span class="block text-xs font-medium text-slate-700">
          Date column
        </span>
        <select
          v-model="recordDateColumn"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white">
          <option disabled value="">-- Select --</option>
          <option
            v-for="h in props.state.config.headers"
            :key="h"
            :value="h">
            {{ h }}
          </option>
        </select>
      </div>

      <!-- Date format -->
      <div>
        <span class="block text-xs font-medium text-slate-700">
          Date format
        </span>
        <select
          v-model="recordDateFormat"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white">
          <option disabled value="">-- Select --</option>
          <option
            v-for="f in dateFormats"
            :key="f"
            :value="f">
            {{ f }}
          </option>
        </select>
        <p class="mt-1 text-[11px] text-slate-400">
          e.g. <code>yyyy-MM-dd</code>, <code>MM/dd/yyyy</code>
        </p>
      </div>

      <!-- symbol column -->
      <div>
        <span class="block text-xs font-medium text-slate-700">
          Symbol column
        </span>
        <select
          v-model="symbol"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-800 outline-none focus:border-slate-400 focus:bg-white">
          <option disabled value="">-- Select --</option>
          <option
            v-for="h in props.state.config.headers"
            :key="h + '-symbol'"
            :value="h">
            {{ h }}
          </option>
        </select>
        <p class="mt-1 text-[11px] text-slate-400">
          Optional. Used to filter by symbols in dashboard.
        </p>
      </div>
    </div>

    <!-- no headers -->
    <p
      v-else
      class="mt-3 text-xs text-slate-400">
      Please upload a file and detect headers before configuring date & symbol columns.
    </p>
  </div>
</template>