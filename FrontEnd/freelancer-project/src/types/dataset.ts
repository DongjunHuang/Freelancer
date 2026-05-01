export type ColumnType = 'UNKNOWN' | 'DATE' | 'NUMBER' | 'STRING'

export interface ColumnMeta {
  columnName: string
  dataType: ColumnType
  metric: boolean
}

export interface Dataset {
  datasetId: string | null
  datasetName: string | null
  headers: ColumnMeta[]
  rowCount: number
}

// Data Points data types
export interface Datapoint {
  recordedTime: string // The recorded date
  symbol: string // The symbol
  column: string // The column
  value: number | null // The real datapoints
}

// =====================================DTO======================================
export interface CreateDatasetReq {
  datasetName: string
  recordTimeColumnName: string
  recordTimeColumnFormat?: string
  recordPrimaryIndexedColumnName: string
  timezone: string
}

export interface CreateDatasetResp {
  result: string
  jobId: string
  status: string
  datasetName: string
}

export interface AppendDatasetReq {
  datasetName: string
}

export interface GetUserDatasetsResp {
  datasets: Dataset[]
}

export interface QueryRecordsReq {
  startDate: string
  endDate: string
  columns: string[]
  symbols: string
}

export interface QueryRecordsResp {
  datasetName: string // The dataset name
  columns: string[] // The selected columns
  records: Datapoint[] // The datapoints for the corresponding columns
}
