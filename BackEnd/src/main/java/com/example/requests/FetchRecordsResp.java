package com.example.requests;

import java.util.List;

import lombok.Data;

@Data
public class FetchRecordsResp {
    private String datasetName;
    private List<String> columns;
    private List<DataPoint> datapoints;
}
