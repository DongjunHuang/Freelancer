export enum DashboardEvents {
  UpdateFilters = 'update:filters',
  Generate = 'generate',
}

export enum UploadEvents {
  UpdateState = 'update:state', 
  StartUpload = 'upload:start',   
  CancelUpload = 'upload:cancel', 
  RetryUpload = 'upload:retry',   
}