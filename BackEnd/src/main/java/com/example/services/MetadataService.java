package com.example.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.exception.BadRequestException;
import com.example.exception.ErrorCode;
import com.example.guards.DatasetAction;
import com.example.guards.DatasetStateGuard;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetRecordRepo;
import com.example.repos.DatasetStatus;

import lombok.RequiredArgsConstructor;

/**
 * Metadata service to provide functions for datasets.
 */
@Service
@RequiredArgsConstructor
public class MetadataService {
    private final DatasetRecordRepo recordRepo;
    private final DatasetStateGuard stateGuard;
    private final DatasetMetadataRepo datasetRepo;

    /**
     * Get the user datasets by user id.
     * 
     * @param userId the user id.
     * @return the datasets found for the user.
     */
    public List<DatasetMetadata> getUserDatasets(Long userId) {
        return datasetRepo.findByUserId(userId);
    }

    /**
     * Delete the datasets and datapoints belongs to the datapoints.
     * 
     * @param datasetId
     */
    public void deleteDatasetByNameAndUserId(String name, Long userId) {
        DatasetMetadata dataset = stateGuard.loadAndCheck(userId, name, DatasetAction.DELETE);

        // Notify the deleting status of the dataset
        datasetRepo.save(dataset);

        try {
            // Remove datapoints
            recordRepo.deleteByDatasetId(dataset.getId());

            // Remove dataset
            datasetRepo.delete(dataset);
        } catch (Exception ex) {
            if (dataset != null) {
                dataset.setStatus(DatasetStatus.FAILED);
                dataset.setLastErrorCode(ErrorCode.DELETE_FAILED);
                dataset.setLastErrorMessage(ex.getMessage());
                datasetRepo.save(dataset);
            }
            throw new BadRequestException(ErrorCode.DELETE_FAILED);
        }
    }
}
