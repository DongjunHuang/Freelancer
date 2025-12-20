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
import { ref, onMounted, computed, reactive, onBeforeUnmount} from 'vue'
import { uploadCsv, fetchDatasets, deleteDataset} from '@/api/upload' 
import { useMessage } from 'naive-ui'


// Consts: global params
const uploadState = reactive<UploadState>(createInitialUploadState())
const datasets = ref<Dataset[]>([])
const message = useMessage()
const uploadStatusEl = ref<HTMLElement | null>(null)

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
const failed = ref<boolean>(false)
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
  failed.value = false

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
    failed.value = true
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

onBeforeUnmount(() => {
  document.removeEventListener('click', onGlobalClick)
})

onMounted(async () => {
  document.addEventListener('click', onGlobalClick)
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

function clearUploadStatus() {
  progress.value = 0
  failed.value = false
  successMsg.value = ''
  errorMsg.value = ''
}

function onGlobalClick(e: MouseEvent) {
  if (!uploadStatusEl.value) {
    return
  }
    
  if (!uploadStatusEl.value.contains(e.target as Node)) {
    clearUploadStatus()
  }
}

</script>

<template>
  <div
    ref="uploadStatusEl" 
    class="max-w-xl mx-auto">
    <!-- Select or add table name-->
    <div>
      <DatasetSelectionPane
        :state="uploadState"
        :datasets="datasets"
        @update:state="updateState"
        @delete-dataset="handleDeleteDataset"
        @load-datasets="loadDatasets"/>
    </div>

    <!-- Drop the files to be uploaded-->
    <div class="pt-4">
      <UploadFilePane
        :state="uploadState"
        @update:state="updateState"/>
    </div>


    <!--Select required upload file config info--> 
    <div class="pt-4" v-show="uploadState.dataset.isNew">
      <UploadFileConfigPane
        :state="uploadState"
        @update:state="updateState"/>  
    </div>
    
    <!--Submit button--> 
    <div class="pt-4 flex items-center gap-3 relative">
      <!-- Upload button -->
      <button
        :disabled="!isUploading && !canUpload"
        @click="isUploading ? cancelUpload() : startUpload()"
        class="px-4 py-2 rounded-lg text-xs font-medium transition-colors disabled:opacity-50"
        :class="isUploading
          ? 'bg-red-500 text-white hover:bg-red-600'
          : 'bg-black text-white hover:bg-slate-800'">
        {{ isUploading ? 'Cancel' : 'Upload' }}
      </button>

      <!-- Status badge -->
      <span
        v-if="isUploading || progress > 0 || failed"
        class="flex items-center justify-center
              h-5 min-w-[20px] px-1
              rounded-full text-[11px] font-semibold
              transition-all duration-300"
        :class="{
          'bg-slate-100 text-slate-700': progress < 100 && !failed,
          'bg-emerald-100 text-emerald-600': progress === 100 && !failed,
          'bg-red-100 text-red-600 ring-2 ring-red-400': failed
        }">
        <template v-if="progress < 100 && !failed">
          {{ progress }}%
        </template>
        <template v-else-if="progress === 100 && !failed">
          ✓
        </template>
        <template v-else>
          ✕
        </template>
      </span>

      <!-- Error message -->
      <p v-if="errorMsg" class="text-red-500 text-sm ml-2">
        {{ errorMsg }}
      </p>
    </div>
  </div>
</template>