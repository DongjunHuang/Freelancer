package com.example.dataset.app;

import com.example.dataset.domain.DatasetMetadata;
import com.example.dataset.infra.mongo.DatasetMetadataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatasetService {
    private final DatasetMetadataRepo datasetRepo;

    /**
     * Get user datasets.
     *
     * @param userId the user id.
     * @return the dataset metadata.
     */
    public List<DatasetMetadata> getUserDatasets(Long userId) {
        return datasetRepo.findByUserIdAndObsoletedFalse(userId);
    }

    /**
     * TODO: Delete the dataset should trigger job.
     * @param name
     * @param userId
     */
    public void deleteDatasetByNameAndUserId(String name, Long userId) {
        // TODO : we are using async deletion
    }

}
