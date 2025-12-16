// Data sets data types
export interface DatasetReq {
    datasetName: string;
    recordDateColumnName: string;
    recordDateColumnFormat: string;
    recordSymbolColumnName: string;
    newDataset: boolean;
}

export interface ColumnMeta {
    columnName: string;
    dataType: ColumnType;
    metric: boolean;
}
  
export type ColumnType = 'UNKNOWN' | 'DATE' | 'NUMBER' | 'STRING';
  
export interface Dataset {
    datasetName: string;
    headers: ColumnMeta[];
    rowCount: number;
}

// Data Points data types
export interface DataPoint {
    recordDate: string;          // The recorded date
    symbol: string;              // The symbol
    column: string;              // The column
    value: number | null;  // The real datapoints
}

export interface FetchRecordsResp {
    datasetName: string;    // The dataset name
    columns: string[];      // The selected columns  
    datapoints: DataPoint[]    // The datapoints for the corresponding columns          
}

export interface Series { 
    key: string; 
    label: string; 
    points: Array<number | null> 
}

export interface ChartData {
    labels: string[]
    symbols: string[]
    seriesMap: Record<string, Array<number | null>>
  }