package com.example.dataset.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class VersionControl {
    private Integer version;
    private List<ColumnMeta> headers;
    private Long rowCount;
}