<!-- Upload.vue -->
<script setup lang="ts">
import { computed, ref,reactive } from 'vue'
import DateConfig from '@/Components/DateConfig.vue';
import UploadTray from '@/Components/UploadTray.vue';

import type { DatasetReq } from '@/api/types';
import { uploadCsv, uploadCsvSimulate } from '@/api/upload'  
import axios from 'axios'

const tables = ref<{ id: string; name: string; rowCount: number }[]>([])
const selectedMode = ref<'existing' | 'new'>('existing')
const selectedTable = ref('')
const status = ref<'idle' | 'uploading' | 'done' | 'error' | 'cancelled'>('idle')
const headers = ref<string[]>([])

// Confirmed 
const recordDateColumn = ref('');
const recordDateFormat = ref('');

const file = ref<File | null>(null)
const error = ref('')
const newTable = ref('')
  
const successMsg = ref('')
const uploading = ref(false)
const isDragging = ref(false)  

// UploadTray related properties
const tray = reactive({
  visible: false,
  filename: '',
  progress: 0,
  status: 'idle' as 'idle' | 'uploading' | 'done' | 'error',
});
let abortCtrl: AbortController | null = null;

// The action to upload files
function onDragOver() { isDragging.value = true }
function onDragLeave() { isDragging.value = false }
function onDrop(e: DragEvent) {
  isDragging.value = false
  const f = e.dataTransfer?.files?.[0]
  if (f) handleFile(f)
}

// The file input field
const fileInput = ref<HTMLInputElement | null>(null)
function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement  
  const f = target.files?.[0]
  if (f) {
    handleFile(f)
  }
}

async function handleFile(f: File) {
  if (!f.name.endsWith('.csv')) {
    error.value = 'Please select a CSV file'
    file.value = null
    headers.value = []
    return
  }

  if (f.size > 5 * 1024 * 1024) {
    error.value = 'File too large (max 5MB)'
    file.value = null
    headers.value = []
    return
  }

  error.value = ''
  file.value = f
  headers.value = await extractHeaders(f)

  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function clearFile() {
  file.value = null
  error.value = ''
  successMsg.value = ''
  headers.value = []

  if (fileInput.value) {
    fileInput.value.value = ''
  }
}


// Check any thing wrong in the uploading area
const canUpload = computed(() => {
  const tableOk = selectedMode.value === 'existing'
      ? !!selectedTable.value
      : !!newTable.value
  return tableOk && !!file.value
})

// Upload action
async function upload() {
  if (!canUpload.value) 
    return

  tray.visible = true;
  tray.progress = 0;
  tray.status = 'uploading';

  error.value = ''
  successMsg.value = ''
  uploading.value = true
  abortCtrl = new AbortController();

  // Create form data
  const fd = new FormData();
  if (file.value) {
    fd.append('file', file.value)
  } else {
    alert('Please upload file first')
    return
  }
  const tableName =
    selectedMode.value === 'existing'
      ? selectedTable.value
      : newTable.value.trim()

  const dataset: DatasetReq = {
    datasetName: tableName,
    recordDateColumnName: recordDateColumn.value,
    recordDateColumnFormat: recordDateFormat.value,
    newDataset: selectedMode.value === 'new', 
  };
  console.log("datesetName {}, recordDateColumnName name {}, recordDateColumnFormat name {}, is new {}", tableName, recordDateColumn.value, recordDateFormat.value, dataset.newDataset)
  try {
    await uploadCsv(file.value, dataset, {
      onProgress: (pct) => {
        tray.progress = pct;
      },
        signal: abortCtrl.signal,
    });
    /*
    await uploadCsvSimulate(fd, {
      signal: abortCtrl.signal,
      onProgress: (pct) => (tray.progress = pct),
    })*/

    tray.status = 'done'
  } catch (e) {
    if (axios.isCancel(e)) 
      status.value = 'cancelled'
    else 
      status.value = 'error'
  } finally {
    abortCtrl = null
  }
}

function onCancelUpload() {
  if (abortCtrl) {
    abortCtrl.abort();
    abortCtrl = null;
  }
  tray.status = 'idle';
  tray.visible = false;
  tray.progress = 0;
}

function onCloseTray() {
  tray.visible = false;
}

async function extractHeaders(file: File): Promise<string[]> {
  const text = await file.text();

  const firstLine = text.split(/\r?\n/)[0];

  const headers = firstLine
    .split(',')
    .map(h => h.trim())
    .filter(h => h.length > 0);
    return ['N/A', ...headers];
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

    <!-- The uploading progress bar-->
    <div class="mt-4 flex items-center gap-3">
      <button
        class="px-3 py-2 rounded-xl border"
        :disabled="uploading"
        @click="clearFile">
        Clear
      </button>
    </div>

    <!-- Select or add table name-->
    <div class="pt-4 grid gap-4 sm:grid-cols-2">
      <div>
        <select v-model="selectedMode" class="w-full border rounded-lg p-2">
          <option value="existing">Select from existing table.</option>
          <option value="new">Create new table.</option>
        </select>
      </div>

      <!-- Select table or add new table field -->
      <div>
        <!-- Select existing table -->
        <select
          v-if="selectedMode === 'existing'"
          v-model="selectedTable"
          class="w-full border rounded-lg p-2">

          <option disabled value="">Select</option>
          <option v-for="t in tables" :key="t.id" :value="t.name">
            {{ t.name }} ({{ t.rowCount }} rows)
          </option>
        </select>

        <!-- Create new table -->
        <input
          v-else
          v-model.trim="newTable"
          placeholder="eg: sales_2025_q4"
          class="w-full border rounded-lg p-2"
        />
      </div>
    </div>
    
    <!--Select required time info--> 
    <DateConfig :headers="headers"
      v-model:column="recordDateColumn"
      v-model:format="recordDateFormat"  /> 
    
    <!--Submit button--> 
    <div class="flex items-center gap-3">
        <button :disabled="!canUpload" @click="upload"
              class="px-4 py-2 rounded-lg bg-black text-white disabled:opacity-50">
        Upload
        </button>
    </div>
    <p v-if="successMsg" class="mt-3 text-sm text-green-600">{{ successMsg }}</p>
    <!--Uploading progress bar--> 
    <UploadTray
      :visible="tray.visible"
      :filename="tray.filename"
      :progress="tray.progress"
      :status="tray.status"
      @cancel="onCancelUpload"
      @close="onCloseTray"
    />
  </div>
</template>