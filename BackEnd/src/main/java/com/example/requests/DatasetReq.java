package com.example.requests;

import lombok.Data;

@Data
public class DatasetReq {
    private String datasetName;
    private String recordDateColumnName;
    private String recordDateColumnFormat;
    private boolean newDataset;
}
