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
    recordTimeColumn: string
    recordTimeFormat: string
    recordPrimaryIndexedColumnName: string
    headers: string[]
    timezone: string
  }
}

export function createInitialUploadState(): UploadState {
  return {
    dataset: {
      isNew: true, // Default：create new dataset
      selectedName: '', // Select existed
      newName: '', // User input
    },
    file: null, // Not select file
    error: '', // No error

    config: {
      delimiter: ',', // The separator of CSV file
      hasHeader: true, // The first row is header
      recordTimeColumn: '', // User select
      recordTimeFormat: '', // User select
      recordPrimaryIndexedColumnName: '', // User select
      headers: [], // Headers
      timezone: '', // timezone
    },
  }
}

export type UploadStatePatch = {
  dataset?: Partial<UploadState['dataset']>
  config?: Partial<UploadState['config']>
  file?: UploadState['file']
  error?: UploadState['error']
}
