package com.example.common.dataset.infra.mongo;

import com.example.common.dataset.domain.DatasetMetadata.VersionControl;
import com.example.upload.domain.DatasetStatus;

public interface DatasetMetadataRepoCustom {
    void updateStagedHeaders(String datasetId, VersionControl staged, DatasetStatus status);

    void promoteStagedToCurrent(String datasetId);

    void discardStaged(String datasetId);
}
