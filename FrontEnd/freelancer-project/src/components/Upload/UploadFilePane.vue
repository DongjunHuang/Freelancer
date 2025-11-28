<script setup lang="ts">
import {ref} from 'vue'
import { UploadEvents } from '@/constants/events'
import type { UploadState } from '@/composables/UploadComposable'

// Only read props
const props = defineProps<{
  state: UploadState,
}>()

// Change events
const emit = defineEmits<{
  'update:state': [UploadState]
}>()

const isDragging = ref(false)  
const error = ref('')
const uploading = ref(false)


// The action to upload files
function onDragOver() { isDragging.value = true }
function onDragLeave() { isDragging.value = false }
function onDrop(e: DragEvent) {
  isDragging.value = false
  const f = e.dataTransfer?.files?.[0]
  if (f) 
    handleFile(f)
}

const fileInput = ref<HTMLInputElement | null>(null)

function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement  
  const f = target.files?.[0]
  if (f) {
    handleFile(f)
  }
}

async function handleFile(f: File) {
  // If not csv file
  if (!f.name.endsWith('.csv')) {
    updateState({
      error: 'Please select a CSV file',
      file: null,
      headers: []
    })
    return
  }

  // If over 5 MB size of file
  if (f.size > 5 * 1024 * 1024) {
    updateState({
      error: 'File too large (max 5MB)',
      file: null,
      headers: []
    })
    return
  }

  // Clear state
  updateState({
    error: '',
    file: f
  })

  const extracted = await extractHeaders(f)

  // Set up headers
  updateState({
    headers: extracted
  })

  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function clearFile() {
  updateState({
    file: null,
    error: '',
    headers: []
  })

  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// Extract headers from the corresponding datasets
async function extractHeaders(file: File): Promise<string[]> {
  const text = await file.text();

  const firstLine = text.split(/\r?\n/)[0];

  const headers = firstLine
    .split(',')
    .map(h => h.trim())
    .filter(h => h.length > 0);
    return [...headers];
}

function updateState(patch: Partial<UploadState>) {
  emit(UploadEvents.UpdateState, {
    ...props.state,
    ...patch
  })
}
</script>

<template>
  <label
    class="flex flex-col items-center justify-center w-full h-44 border-2 border-dashed rounded-2xl cursor-pointer transition bg-white hover:bg-gray-50"
    :class="[isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300', error ? 'border-red-500' : '']"
    @dragover.prevent="onDragOver"
    @dragleave.prevent="onDragLeave"
    @drop.prevent="onDrop"
    role="button"
    tabindex="0">

    <input
      ref="fileInput"
      type="file"
      class="hidden"
      accept=".csv,text/csv"
      @change="onFileChange"/>

    <div class="text-center px-6">
      <div class="text-sm text-gray-700">
        Drag CSV here or
        <span class="text-blue-600 underline">select</span>
      </div>
      <div class="mt-2 text-xs text-gray-500">max 5MB, only CSV file</div>

      <div
        v-if="props.state.file"
        class="mt-4 flex items-center justify-center gap-3 bg-gray-50 border rounded-lg py-2 px-4 shadow-sm">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-8 w-8 text-blue-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2">
        
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M7 21h10a2 2 0 002-2V7l-5-5H7a2 2 0 00-2 2v16a2 2 0 002 2z"/>
        </svg>

        <div class="text-left">
          <p class="text-sm font-medium text-gray-800 truncate max-w-xs">
            {{ props.state.file.name }}
          </p>
          <p class="text-xs text-gray-500">
            {{ (props.state.file.size / 1024 / 1024).toFixed(2) }} MB
          </p>
        </div>
      </div>
    </div>
  </label>

  <p v-if="error" class="mt-2 text-sm text-red-600">{{ error }}</p>

  <!-- The uploading progress bar-->
  <div class="mt-4 flex items-center gap-3">
    <button
    class="px-3 py-2 rounded-xl border"
    :disabled="uploading"
    @click="clearFile">
    Clear
    </button>
  </div>
</template>