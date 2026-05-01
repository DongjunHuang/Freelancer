package com.example.dataset.domain.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetMetadataResp {
    List<Dataset> datasets;
}
