import http from './http'
import type { DatasetReq } from '@/api/types';

// uploadCsv.ts
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