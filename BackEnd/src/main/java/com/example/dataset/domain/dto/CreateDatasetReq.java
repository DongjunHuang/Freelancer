package com.example.dataset.domain.dto;

import lombok.Data;

@Data
public class CreateDatasetReq {
    private String datasetName;
    private String recordTimeColumnName;
    private String recordTimeColumnFormat;
    private String recordPrimaryIndexedColumnName;
    private String timezone;
}
