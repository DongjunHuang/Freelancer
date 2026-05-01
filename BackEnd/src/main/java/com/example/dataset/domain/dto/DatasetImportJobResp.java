package com.example.dataset.domain.dto;

import lombok.Data;

@Data
public class DatasetImportJobResp {
    private String jobId;
    private Long datasetId;
    private String status;
    private String stage;
    private Long totalRows;
    private Long processedRows;
    private Long successRows;
    private Long failedRows;
    private String errorCode;
    private String errorMessage;
}
