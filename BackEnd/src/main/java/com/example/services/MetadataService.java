package com.example.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetRecordRepo;

import lombok.RequiredArgsConstructor;

/**
 * Metadata service to provide functions for datasets.
 */
@Service
@RequiredArgsConstructor
public class MetadataService {
    private final DatasetMetadataRepo metadataRepo;
    private final DatasetRecordRepo recordRepo;

    /**
     * Get the user datasets by user id.
     * 
     * @param userId the user id.
     * @return the datasets found for the user.
     */
    public List<DatasetMetadata> getUserDatasets(Long userId) {
        return metadataRepo.findByUserId(userId);
    }

    /**
     * Delete the datasets and datapoints belongs to the datapoints.
     * 
     * @param datasetId
     */
    public void deleteDatasetByNameAndUserId(String name, Long userId) {
        DatasetMetadata ds = metadataRepo.findByUserIdAndDatasetName(userId, name)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATASET_NOT_FOUND));

        // Remove datapoints
        recordRepo.deleteByDatasetId(ds.getId());

        // Remove dataset
        metadataRepo.delete(ds);
    }
}
