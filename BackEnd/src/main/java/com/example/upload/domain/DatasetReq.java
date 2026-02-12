package com.example.upload.domain;

import lombok.Data;

@Data
public class DatasetReq {
    private String datasetName;
    private String recordDateColumnName;
    private String recordDateColumnFormat;
    private String recordSymbolColumnName;
    private boolean newDataset;
}
