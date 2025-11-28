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
    recordDate: string;                          
    values: Record<string, number | string | null>;
}
  
export interface FetchRecordsResp {
    datasetName: string;
    columns: string[];                           
    datapoints: Record<string, DataPoint[]>                    
}

export interface Series { 
    key: string; 
    label: string; 
    points: Array<number | null> 
}
