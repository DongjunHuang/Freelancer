<!-- Upload.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { uploadCsv } from '@/api/upload'  
import type { AxiosError } from 'axios' 

const file = ref<File | null>(null)
const error = ref('')
const successMsg = ref('')
const uploading = ref(false)
const isDragging = ref(false)

function onDragOver() { isDragging.value = true }
function onDragLeave() { isDragging.value = false }
function onDrop(e: DragEvent) {
  isDragging.value = false
  const f = e.dataTransfer?.files?.[0]
  if (f) handleFile(f)
}

function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement  
  const file = target.files?.[0]
  if (file) handleFile(file)
}

function handleFile(f: File) {
  if (!f.name.endsWith('.csv')) {
    error.value = 'Please select a CSV file'
    file.value = null
    return
  }
  if (f.size > 5 * 1024 * 1024) {
    error.value = 'File too large (max 5MB)'
    file.value = null
    return
  }
  error.value = ''
  file.value = f
}

function clearFile() {
  file.value = null
  error.value = ''
  successMsg.value = ''
}

async function upload() {
  if (!file.value) 
    return
  error.value = ''
  successMsg.value = ''
  uploading.value = true
    
  try {
    const fd = new FormData()
    fd.append('file', file.value)
    const { data } = await uploadCsv(fd)
    successMsg.value = data?.message ?? 'Upload successfully.'
  } catch (e) {
    const err = e as AxiosError<any>
    error.value =
      err.response?.data?.error ||
      err.response?.data?.message ||
      err.message ||
      'Upload failed.'
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="max-w-xl mx-auto">
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
          v-if="file"
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
              {{ file.name }}
            </p>
            <p class="text-xs text-gray-500">
              {{ (file.size / 1024 / 1024).toFixed(2) }} MB
            </p>
          </div>
        </div>
      </div>
    </label>

    <p v-if="error" class="mt-2 text-sm text-red-600">{{ error }}</p>

    <div class="mt-4 flex items-center gap-3">
      <button
        class="px-4 py-2 rounded-xl bg-blue-600 text-white disabled:opacity-50"
        :disabled="!file || !!error || uploading"
        @click="upload">
        {{ uploading ? 'Uploading...' : 'Upload' }}
      </button>

      <button
        class="px-3 py-2 rounded-xl border"
        :disabled="uploading"
        @click="clearFile">
        Clear
      </button>
    </div>

    <p v-if="successMsg" class="mt-3 text-sm text-green-600">{{ successMsg }}</p>
  </div>
</template>

<!--
<div v-if="uploading" class="mt-3 w-full bg-gray-200 rounded-full h-2">
  <div class="h-2 rounded-full bg-blue-600" :style="{ width: progress + '%' }"></div>
</div>
-->