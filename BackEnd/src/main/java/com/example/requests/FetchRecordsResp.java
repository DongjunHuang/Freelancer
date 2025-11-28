package com.example.requests;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class FetchRecordsResp {
    private String datasetName;
    private List<String> columns;
    private Map<String, List<DataPoint>> datapoints;
}
