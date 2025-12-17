package com.example.repos;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatasetMetadataRepo extends MongoRepository<DatasetMetadata, String>, DatasetMetadataRepoCustom {
    /**
     * Find the dataset according to the user id and dataset name.
     * 
     * @param userId      the user id.
     * @param datasetName the dataset name.
     * 
     * @return the found dataset.
     */
    Optional<DatasetMetadata> findByUserIdAndDatasetName(Long userId, String datasetName);

    /**
     * Find list of datasets metadata according to the user id.
     * 
     * @param userId the user id.
     * 
     * @return the list of datasets.
     */
    List<DatasetMetadata> findByUserId(Long userId);

    /**
     * Find list of datasets according tot he statuses. This will be used mostly
     * with the background thread to check
     * abnormal datasets periodically.
     * 
     * @param statuses the statuses to be checked.
     * @param cutoff   the time limit.
     * 
     * @return the list of the datasets found.
     */
    List<DatasetMetadata> findByStatusInAndUpdatedAtBefore(List<DatasetStatus> statuses, Instant cutoff);
}