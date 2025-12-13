<!-- Upload.vue -->
<script setup lang="ts">
// Import components
import UploadFileConfigPane from '@/components/Upload/UploadFileConfigPane.vue'
import UploadFilePane from '@/components/Upload/UploadFilePane.vue'
import DatasetSelectionPane from '@/components/Upload/DatasetSelectionPane.vue'

// Import Types 
import type { UploadState, UploadStatePatch } from '@/composables/UploadComposable'
import type { DatasetReq, Dataset } from '@/api/types';

// Import functions
import { createInitialUploadState } from '@/composables/UploadComposable'
import { ref, onMounted, computed, reactive } from 'vue'
import { uploadCsv, fetchDatasets, deleteDataset} from '@/api/upload' 
import { useMessage } from 'naive-ui'

// Consts: global params
const uploadState = reactive<UploadState>(createInitialUploadState())
const datasets = ref<Dataset[]>([])
const message = useMessage()

// Consts: UI status
const loading = ref(false)
const error = ref('')
const successMsg = ref('')
const errorMsg = ref('')

// Consts: The params of uploading circle
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
  const { dataset, file, error } = uploadState

  const hasFile = !!file

  const usingExistingOk =
    !dataset.isNew && !!dataset.selectedName

  const creatingNewOk =
    dataset.isNew && dataset.newName.trim().length > 0

  const datasetOk = usingExistingOk || creatingNewOk

  return hasFile && datasetOk && !error
})

// Functions
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
    return
  }


  if (!uploadState.dataset.selectedName && !uploadState.dataset.newName) {
      return
  }
    
  //TODO: Change to add new params
  const datasetReq: DatasetReq = {
    datasetName: uploadState.dataset.isNew ? uploadState.dataset.newName 
              : uploadState.dataset.selectedName,
    recordDateColumnName: uploadState.config.recordDateColumn,
    recordDateColumnFormat: uploadState.config.recordDateFormat,
    recordSymbolColumnName: uploadState.config.symbol,
    newDataset: uploadState.dataset.isNew 
  };

  console.log(datasetReq)

  try {
    await uploadCsv(uploadState.file, datasetReq, {
      onProgress: (pct) => {
        progress.value = pct;
      },
        signal: abortCtrl.signal,
    });
  } catch (e: any) {
    const detail  = e?.response?.data?.detail
    errorMsg.value = detail
  } finally {
    abortCtrl = null
    isUploading.value = false
  }
}

function cancelUpload() {
  console.log("Cancel upload")
  abortCtrl?.abort()
  isUploading.value = false
  showProgress.value = false
  progress.value = 0
}

// Load datasets
async function loadDatasets() {
  try {
    loading.value = true
    const resp = await fetchDatasets() 
    
    // Assign the datasets locally
    datasets.value = resp   

    // The default is the first of the datasets
    if (datasets.value.length > 0) {
      uploadState.dataset.selectedName = datasets.value[0].datasetName
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


async function handleDeleteDataset(name: string) {
  try {
    await deleteDataset(name)
    message.success(`Dataset "${name}" deleted`)

    datasets.value = datasets.value.filter(d => d.datasetName !== name)

    if (uploadState.dataset.selectedName === name) {
      uploadState.dataset.selectedName = ''
    }
  } catch (err) {
    console.error(err)
    message.error(`Failed to delete dataset "${name}"`)
  }
}

function updateState(next: UploadStatePatch) {
  if (next.dataset) {
    Object.assign(uploadState.dataset, next.dataset)
  }
    
  if (next.config) 
    Object.assign(uploadState.config, next.config)
  
  if (next.file !== undefined) {
    uploadState.file = next.file
  } 

  if (next.error !== undefined) {
    uploadState.error = next.error
  } 
}
</script>

<template>
  <div class="max-w-xl mx-auto">
    <!-- Select or add table name-->
    <div>
      <DatasetSelectionPane
        :state="uploadState"
        :datasets="datasets"
        @update:state="updateState"
        @delete-dataset="handleDeleteDataset"/>
    </div>

    <!-- Drop the files to be uploaded-->
    <div class="pt-4">
      <UploadFilePane
        :state="uploadState"
        @update:state="updateState"/>
    </div>


    <!--Select required upload file config info--> 
    <div class="pt-4">
      <UploadFileConfigPane
        :state="uploadState"
        @update:state="updateState"/>
    </div>
    
    <!--Submit button--> 
    <p v-if="successMsg" class="mt-3 text-sm text-green-600">{{ successMsg }}</p>
    <div class="pt-4 flex items-center gap-3">
      <div class="pt-4 flex items-center gap-3">
        <button     
          :disabled="!isUploading && !canUpload"
          @click="isUploading ? cancelUpload() : startUpload()"
          class="px-4 py-2 rounded-lg text-xs font-medium transition-colors disabled:opacity-50"
          :class="isUploading
            ? 'bg-red-500 text-white hover:bg-red-600'
            : 'bg-black text-white hover:bg-slate-800'">
          {{ isUploading ? 'Cancel' : 'Upload' }}
        </button>
      </div>
      <p v-if="errorMsg" class="text-red-500 text-sm">
        {{ errorMsg }}
      </p>
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
            stroke-linecap="round"/>
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