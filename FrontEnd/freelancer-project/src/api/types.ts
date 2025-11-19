export interface DatasetReq {
    datasetName: string;
    recordDateColumnName: string;
    recordDateColumnFormat: string;
    newDataset: boolean;
}

export interface ColumnMeta {
    columnName: string;
    dataType: ColumnType;
    isMetric: boolean;
}
  
export type ColumnType = 'UNKNOWN' | 'DATE' | 'NUMBER' | 'STRING';
  
export interface Dataset {
    datasetName: string;
    headers: ColumnMeta[];
    rowCount: number;
}