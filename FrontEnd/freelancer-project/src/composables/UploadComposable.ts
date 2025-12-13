export interface UploadState {
  // data set
  dataset: {
    isNew: boolean          
    selectedName: string
    newName: string        
  }

  // File upload
  file: File | null
  error: string

  // File config
  config: {
    delimiter: string
    hasHeader: boolean
    recordDateColumn: string
    recordDateFormat: string
    symbol: string
    headers: string[]
  }
}

export function createInitialUploadState(): UploadState {
  return {
    dataset: {
      isNew: true,            // Default：create new dataset
      selectedName: '',     // Select existed
      newName: ''             // User input
    },
    file: null,               // Not select file
    error: '',                // No error

    config: {
      delimiter: ',',         // The separator of CSV file
      hasHeader: true,        // The first row is header
      recordDateColumn: '',   // User select
      recordDateFormat: '',   // User select
      symbol: '',             // User select
      headers: []             // Headers
    }
  }
}

export type UploadStatePatch = {
  dataset?: Partial<UploadState['dataset']>
  config?: Partial<UploadState['config']>
  file?: UploadState['file']
  error?: UploadState['error']
}
