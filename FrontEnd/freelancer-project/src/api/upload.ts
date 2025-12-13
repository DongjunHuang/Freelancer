import http from './http'
import type { DatasetReq, Dataset } from '@/api/types';

// The api to upload csv file
export const uploadCsv = (
  file: File,
  dataset: DatasetReq,
  config?: {
    onProgress?: (pct: number) => void;
    signal?: AbortSignal;
  }
) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append(
    'dataset',
    new Blob([JSON.stringify(dataset)], { type: 'application/json' }),
  );
  
  return http.post('/upload/uploadCsv', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    signal: config?.signal,
    onUploadProgress: (e) => {
      if (config?.onProgress && e.total) {
        const pct = Math.round((e.loaded / e.total) * 100);
        config.onProgress(pct);
      }
    },
  });
};

// The api to fetch datasets for the user
export async function fetchDatasets(): Promise<Dataset[]> {
  const res = await http.get<Dataset[]>('/dashboard/fetchDatasets') 
  return res.data
}

// The api to delete dataset
export async function deleteDataset(datasetName: string): Promise<void> {
  const res = await http.delete(`/upload/dataset/${datasetName}`)
  return res.data
}

// ====================================================================
// the simulated function to test the upload functionality.
export const uploadCsvSimulate = async (
  formData: FormData,
  config?: {
    onProgress?: (pct: number) => void;
    signal?: AbortSignal;
  }
) => {
  console.log('⏳ Simulate uploading...');

  let pct = 0;
  return new Promise<void>((resolve, reject) => {
    const timer = setInterval(() => {
      if (config?.signal?.aborted) {
        clearInterval(timer);
        console.log('❌ Cancel Simulating uploading');
        reject(new Error('Upload aborted'));
        return;
      }

      pct += 2;
      config?.onProgress?.(pct);
      if (pct >= 100) {
        clearInterval(timer);
        console.log('✅ Uploading finished');
        resolve();
      }
    }, 100); 
  });
};