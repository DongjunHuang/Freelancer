<!-- Upload.vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUploadState } from '@/composables/UploadComposable'
const { uploadState, datasets } = useUploadState()

// import components
import UploadFileConfigPane from '@/components/Upload/UploadFileConfigPane.vue'
import UploadFilePane from '@/components/Upload/UploadFilePane.vue'
import DatasetSelectionPane from '@/components/Upload/DatasetSelectionPane.vue'

import type { DatasetReq } from '@/api/types';
import { uploadCsv, fetchDatasets} from '@/api/upload' 

// loading and error
const loading = ref(false)
const error = ref('')
const successMsg = ref('')


// The params of uploading circle
let abortCtrl: AbortController | null = null
const progress = ref(0)
const radius = 16
const circumference = 2 * Math.PI * radius
const showProgress = ref(false)
const isUploading = ref(false)
const dashOffset = computed(() =>
  circumference * (1 - progress.value / 100)
)
const canUpload = computed(() => {
  return !!uploadState.file && !!uploadState.selectedDatasetName
})

// Upload action
async function startUpload() {
  if (!canUpload.value) 
    return
  
  isUploading.value = true
  showProgress.value = true
  progress.value = 0
  
  error.value = ''
  successMsg.value = ''
  abortCtrl = new AbortController();

  // Create form data
  const fd = new FormData();
  if (uploadState.file) {
    fd.append('file', uploadState.file)
  } else {
    alert('Please upload file first')
    return
  }

  const datasetReq: DatasetReq = {
    datasetName: uploadState.selectedDatasetName,
    recordDateColumnName: uploadState.recordDateColumn,
    recordDateColumnFormat: uploadState.recordDateFormat,
    recordSymbolColumnName: uploadState.symbol,
    newDataset: uploadState.isNewSelectedMode 
  };

  console.log(datasetReq)

  try {
    await uploadCsv(uploadState.file, datasetReq, {
      onProgress: (pct) => {
        progress.value = pct;
      },
        signal: abortCtrl.signal,
    });
  } catch (e) {
  } finally {
    abortCtrl = null
  }
}

function cancelUpload() {
  abortCtrl?.abort()

  isUploading.value = false
  showProgress.value = false
  progress.value = 0
}

// Load datasets
async function loadDatasets() {
  try {
    loading.value = true
    const resp = await fetchDatasets()   // axios 请求后端
    datasets.value = resp   

    if (datasets.value.length > 0) {
      uploadState.selectedDatasetName = datasets.value[0].datasetName
    }
  } catch (e) {
    console.error(e)
    error.value = 'Failed to load dataset list'
  } finally {
    loading.value = false
  }
}
onMounted(async () => {
  await loadDatasets()
})

</script>

<template>
  <div class="max-w-xl mx-auto">
    <!-- Select or add table name-->
    <div>
      <DatasetSelectionPane
        :state="uploadState"
        :datasets="datasets"
        @update:state="(next) => Object.assign(uploadState, next)"
      />
    </div>

    <!-- Drop the files to be uploaded-->
    <div class="pt-4">
      <UploadFilePane
        :state="uploadState"
        @update:state="(next) => Object.assign(uploadState, next)"
      />
    </div>


    <!--Select required upload file config info--> 
    <div class="pt-4">
      <UploadFileConfigPane
        :state="uploadState"
        @update:state="(next) => Object.assign(uploadState, next)"
      />
    </div>
    
    <!--Submit button--> 
    <p v-if="successMsg" class="mt-3 text-sm text-green-600">{{ successMsg }}</p>
    <div class="pt-4 flex items-center gap-3">
      <div class="pt-4 flex items-center gap-3">
        <button     
          :disabled="!canUpload && !isUploading"
          @click="isUploading ? cancelUpload() : startUpload()"
          class="px-4 py-2 rounded-lg text-xs font-medium transition-colors disabled:opacity-50"
          :class="isUploading
            ? 'bg-red-500 text-white hover:bg-red-600'
            : 'bg-black text-white hover:bg-slate-800'">
          {{ isUploading ? 'Cancel' : 'Upload' }}
        </button>
      </div>
      
      <!-- progress circle -->
      <div v-if="showProgress"
          class="relative mt-1 h-10 w-10 flex items-center justify-center">
        <svg class="h-10 w-10 -rotate-90" viewBox="0 0 40 40">
          <circle
            class="text-slate-200"
            stroke="currentColor"
            stroke-width="4"
            fill="transparent"
            r="16"
            cx="20"
            cy="20"
          />
          <circle
            :class="progress === 100 ? 'text-emerald-500' : 'text-sky-500'"
            stroke="currentColor"
            stroke-width="4"
            fill="transparent"
            r="16"
            cx="20"
            cy="20"
            :stroke-dasharray="circumference"
            :stroke-dashoffset="dashOffset"
            stroke-linecap="round"
          />
        </svg>

        <span
          class="absolute text-[10px] font-semibold"
          :class="progress === 100 ? 'text-emerald-600' : 'text-slate-800'">
          <template v-if="progress < 100">
            {{ progress }}%
          </template>
          <template v-else>
            ✓
          </template>
        </span>
      </div>
    </div>
  </div>
</template>