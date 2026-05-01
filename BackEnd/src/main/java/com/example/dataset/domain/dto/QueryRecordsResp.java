package com.example.dataset.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class QueryRecordsResp {
    private String datasetName;
    private List<String> columns;
    private List<Datapoint> records;
}
