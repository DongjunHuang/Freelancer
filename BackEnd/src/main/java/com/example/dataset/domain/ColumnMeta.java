package com.example.dataset.domain;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ColumnMeta {
    private String columnName;
    private ColumnType dataType;
    private boolean metric;
}