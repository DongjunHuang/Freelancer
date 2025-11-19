package com.example.repos;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatasetMetadataRepo extends MongoRepository<DatasetMetadata, String>, DatasetMetadataRepoCustom {
    DatasetMetadata findByUserIdAndDatasetName(Long userId, String datasetName);
    List<DatasetMetadata> findByUserId(Long userId);
}