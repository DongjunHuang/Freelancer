package com.example.guards;

import org.springframework.stereotype.Component;

import com.example.dataset.domain.DatasetMetadata;
import com.example.dataset.infra.mongo.DatasetMetadataRepo;
import com.example.exception.ErrorCode;
import com.example.exception.types.NotFoundException;

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
            String datasetId,
            DatasetAction action) {

        DatasetMetadata ds = metadataRepo
                .findByUserIdAndId(userId, datasetId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATASET_NOT_FOUND));
        DatasetRules.assertAllowed(action, ds.getStatus());
        return ds;
    }

    public DatasetMetadata check(DatasetAction action, DatasetMetadata dataset) {
        DatasetRules.assertAllowed(action, dataset.getStatus());
        return dataset;
    }
}
