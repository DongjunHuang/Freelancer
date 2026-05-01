package com.example.dataset.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDatasetResp {
    private String result;
    private String jobId;
    private String status;
    private String datasetName;
}