package com.example.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataProps {
    private String recordDateColumnName;
    private String recordDateColumnFormat;
    private Long userId;
    private String batchId;
    private String datasetName;
    private boolean newDataset;
    private String datasetId;
    private int stagedVersion;
}