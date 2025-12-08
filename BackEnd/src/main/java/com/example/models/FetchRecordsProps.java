package com.example.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.example.requests.FetchRecordsReq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FetchRecordsProps {
    private String datasetName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> columns;
    private List<String> symbols;

    public static FetchRecordsProps fromFetchRecordsReq(FetchRecordsReq req) {
        List<String> columnsUpperCase = new ArrayList<>();
        if (req.getColumns() != null) {
            for (int i = 0; i < req.getColumns().size(); i++) {
                columnsUpperCase.add(req.getColumns().get(i).toUpperCase());
            }
        }
        List<String> symbols = null;
        if (req.getSymbols() != null) {
            symbols = Arrays.stream(req.getSymbols().split("[,\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

        }
        FetchRecordsProps fetchRecordsProps = FetchRecordsProps.builder()
                .columns(columnsUpperCase)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .symbols(symbols)
                .datasetName(req.getDatasetName())
                .build();
        return fetchRecordsProps;
    }
}
