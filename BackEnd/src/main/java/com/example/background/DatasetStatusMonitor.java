package com.example.background;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Run the background thread to log the datasets with possible wrong status, in
 * this version we do not do fix,
 * TODO: add fixing logic if find some possible error in the dataset regarding.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatasetStatusMonitor {

    private final DatasetMetadataRepo metadataRepo;
    private final Clock clock;

    private static final Duration UPLOAD_TIMEOUT = Duration.ofMinutes(30);
    private static final Duration DELETE_TIMEOUT = Duration.ofMinutes(10);

    @Scheduled(fixedDelayString = "${dataset.monitor.delay-ms:300000}")
    public void monitorStuckDatasets() {
        Instant now = Instant.now(clock);

        Instant uploadingCutoff = now.minus(UPLOAD_TIMEOUT);
        Instant deletingCutoff = now.minus(DELETE_TIMEOUT);

        logStuck(DatasetStatus.UPLOADING, uploadingCutoff);
        logStuck(DatasetStatus.DELETING, deletingCutoff);
    }

    private void logStuck(DatasetStatus status, Instant cutoff) {
        List<DatasetMetadata> stuck = metadataRepo
                .findByStatusInAndUpdatedAtBefore(List.of(status), cutoff);

        for (DatasetMetadata ds : stuck) {
            log.warn("[DATASET-STUCK] status={}, userId={}, dataset={}, statusUpdatedAt={}",
                    ds.getStatus(), ds.getUserId(), ds.getDatasetName(), ds.getUpdatedAt());
        }
    }
}