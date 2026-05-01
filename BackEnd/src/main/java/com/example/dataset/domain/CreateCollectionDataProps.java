package com.example.dataset.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCollectionDataProps {
    private String recordTimeColumnName;
    private String recordTimeColumnFormat;
    private String recordPrimaryIndexedColumnName;
    private Long userId;
    private String datasetName;
    private String datasetId;
    private int version;
    private String batchId;
    private String timezone;
}