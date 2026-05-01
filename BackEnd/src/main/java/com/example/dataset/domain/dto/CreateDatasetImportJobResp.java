package com.example.dataset.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDatasetImportJobResp {
    private String jobId;
    private Long datasetId;
    private String status;
    private String stage;
}