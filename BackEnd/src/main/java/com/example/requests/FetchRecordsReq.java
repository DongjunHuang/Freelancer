package com.example.requests;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class FetchRecordsReq {
    private String datasetName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<String> columns;
}