package com.example.guards;

import org.springframework.stereotype.Component;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

/**
 * The dataset guard to prevent user from doing corresponding action when the
 * dataset is in the corresponding status.
 */
@Component
@RequiredArgsConstructor
@Builder
public class DatasetStateGuard {
    // The dataset repo
    private final DatasetMetadataRepo metadataRepo;

    public DatasetMetadata loadAndCheck(
            Long userId,
            String datasetName,
            DatasetAction action) {

        DatasetMetadata ds = metadataRepo
                .findByUserIdAndDatasetName(userId, datasetName)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATASET_NOT_FOUND));

        DatasetRules.assertAllowed(action, ds.getStatus());

        return ds;
    }
}
