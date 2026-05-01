package com.example.dataset.infra.mongo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.dataset.domain.DatasetMetadata;
import com.example.dataset.domain.DatasetStatus;

public interface DatasetMetadataRepo extends MongoRepository<DatasetMetadata, String> {
    /**
     * Find the dataset according to the user id and dataset name.
     *
     * @param userId      the user id.
     *
     * @return the found dataset.
     */
    Optional<DatasetMetadata> findByUserIdAndDatasetName(Long userId, String datasetId);

    Optional<DatasetMetadata> findByUserIdAndId(Long userId, String datasetId);


    /**
     * Find list of datasets metadata according to the user id.
     *
     * @param userId the user id.
     *
     * @return the list of datasets.
     */
    List<DatasetMetadata> findByUserIdAndObsoletedFalse(Long userId);

    /**
     * Find list of datasets according to the statuses. This will be used mostly
     * with the background thread to check
     * abnormal datasets periodically.
     * 
     * @param statuses the statuses to be checked.
     * @param cutoff   the time limit.
     * 
     * @return the list of the datasets found.
     */
    List<DatasetMetadata> findByStatusInAndUpdatedAtBeforeAndObsoletedFalse(List<DatasetStatus> statuses, Instant cutoff);
}