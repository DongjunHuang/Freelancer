package com.example.dashboard.domain;

import java.util.List;

import com.example.common.dataset.domain.DatasetMetadata;
import com.example.common.dataset.domain.DatasetMetadata.ColumnMeta;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetMetadataResp {
    private String datasetName;
    private List<ColumnMeta> headers;
    private Long rowCount;

    public static DatasetMetadataResp fromDatasetMetadata(DatasetMetadata metadata) {
        return DatasetMetadataResp.builder()
                .datasetName(metadata.getDatasetName())
                .headers(metadata.getCurrent() == null ? null : metadata.getCurrent().getHeaders())
                .rowCount(metadata.getCurrent() == null ? null : metadata.getCurrent().getRowCount())
                .build();
    }
}
