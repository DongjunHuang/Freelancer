package com.example.dataset.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetImportJobMetadata {
    private final String recordTimeColumnName;
    private final String recordTimeColumnFormat;
    private final String recordPrimaryIndexedColumnName;
    private final String timezone;
}