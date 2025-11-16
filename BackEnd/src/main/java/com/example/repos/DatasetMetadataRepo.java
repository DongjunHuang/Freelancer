package com.example.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatasetMetadataRepo extends MongoRepository<DatasetMetadata, String>, DatasetMetadataRepoCustom {
    Optional<DatasetMetadata> findByUserIdAndDatasetName(Long userId, String datasetName);
    List<DatasetMetadata> findByUserId(Long userId);
}