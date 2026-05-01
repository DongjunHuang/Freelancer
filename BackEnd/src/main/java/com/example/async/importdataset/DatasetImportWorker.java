package com.example.async.importdataset;


import com.example.dataset.domain.DatasetImportJob;
import com.example.dataset.infra.jpa.DatasetImportJobRepository;
import com.example.notification.NotificationEventPublisher;
import com.example.notification.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetImportWorker {
    private final DatasetImportJobRepository importJobRepo;
    private final DatasetCsvImportService datasetCsvImportService;
    private final NotificationEventPublisher notificationEventPublisher;
    private final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    public void process(String jobId) {
        DatasetImportJob job = importJobRepo.findByJobId(jobId)
                .orElseThrow(() -> new IllegalStateException("Import job not found: " + jobId));

        try {
            datasetCsvImportService.createDatasetFromS3(job, new ImportProgressUpdater() {
                @Override
                public void update(long processedRows, long successRows, long failedRows) {
                    importJobRepo.updateProgress(
                            jobId,
                            processedRows,
                            successRows,
                            failedRows,
                            Instant.now()
                    );
                }
            });

            int updated = importJobRepo.markSucceeded(jobId, Instant.now());
            if (updated == 1) {
                NotificationCommand notification = NotificationCommand.builder()
                        .recipientId(job.getUserId())
                        .recipientType(NotificationRecipientType.USER)
                        .content("Dataset " + job.getDatasetName() + " created successfully")
                        .title("Dataset " + job.getDatasetName() + " created successfully")
                        .category(NotificationCategory.SYSTEM)
                        .type(NotificationType.TABLE_CREATED)
                        .sourceType(NotificationSourceType.DATASET)
                        .sourceId(null)
                        .targetType(NotificationTargetType.ISSUE_THREAD)
                        .build();

                notificationEventPublisher.publish(notification);
            }
        } catch (Exception e) {
            importJobRepo.markFailed(
                    jobId,
                    "IMPORT_FAILED",
                    truncateErrorMessage(e.getMessage()),
                    Instant.now()
            );
        }
    }

    private String truncateErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}