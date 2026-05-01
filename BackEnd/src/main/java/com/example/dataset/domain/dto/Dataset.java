package com.example.dataset.domain.dto;

import com.example.dataset.domain.ColumnMeta;
import com.example.dataset.domain.DatasetMetadata;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Dataset {
    private String datasetName;
    private String datasetId;
    private List<ColumnMeta> headers;
    private Long rowCount;

    public static Dataset fromDatasetMetadata(DatasetMetadata metadata) {
        return Dataset.builder()
                .datasetName(metadata.getDatasetName())
                .headers(metadata.getCurrent() == null ? null : metadata.getCurrent().getHeaders())
                .rowCount(metadata.getCurrent() == null ? null : metadata.getCurrent().getRowCount())
                .datasetId(metadata.getId())
                .build();
    }
}
