package com.example.dataset.domain.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class QueryRecordsReq {
    private String datasetName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> columns;
    private String symbols;
}