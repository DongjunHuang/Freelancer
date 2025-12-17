package com.example.repos;

import com.example.repos.DatasetMetadata.VersionControl;

public interface DatasetMetadataRepoCustom {
    void updateStagedHeaders(String datasetId, VersionControl staged, DatasetStatus status);

    void promoteStagedToCurrent(String datasetId);

    void discardStaged(String datasetId);
}
