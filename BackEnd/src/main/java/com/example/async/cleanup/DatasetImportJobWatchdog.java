package com.example.async.cleanup;

import com.example.dataset.domain.DatasetImportJob;
import com.example.dataset.infra.jpa.DatasetImportJobRepository;
import com.example.dataset.infra.mongo.DatasetRecordRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
// TODO: not ready yet
public class DatasetImportJobWatchdog {

    private static final int LIMIT = 20;
    private static final Duration STALE_TIMEOUT = Duration.ofMinutes(10);

    private final DatasetImportJobRepository importJobRepo;
    private final DatasetRecordRepo recordRepo;

    // @Scheduled(fixedDelayString = "${app.jobs.import-watchdog-delay-ms:30000}")
    public void recoverStaleJobs() {
        Instant now = Instant.now();
        Instant deadline = now.minus(STALE_TIMEOUT);
        String watchdogId = watchdogId();

        List<DatasetImportJob> staleJobs = importJobRepo.findStaleRunningJobs(deadline, LIMIT);

        if (staleJobs.isEmpty()) {
            return;
        }

        for (DatasetImportJob job : staleJobs) {
            try {
                // recoverOne(job, watchdogId, deadline, now);
            } catch (Exception ex) {
                log.error("Failed to recover stale import job. jobId={}", job.getJobId(), ex);
            }
        }
    }

    /*
    @Transactional
    protected void recoverOne(DatasetImportJob job,
                              String watchdogId,
                              Instant deadline,
                              Instant now) {
        int claimed = importJobRepo.claimStaleJob(
                job.getId(),
                watchdogId,
                deadline,
                now
        );

        if (claimed != 1) {
            return;
        }

        if (job.getDatasetId() != null && job.get() != null) {
            long deleted = recordRepo.deleteByDatasetIdAndVersion(
                    job.getDatasetId(),
                    job.getStagedVersion()
            );

            log.warn("Cleaned stale staged records. jobId={}, datasetId={}, stagedVersion={}, deleted={}",
                    job.getJobId(), job.getDatasetId(), job.getStagedVersion(), deleted);
        }

        int retryCount = job.getRetryCount() == null ? 0 : job.getRetryCount();
        int maxRetryCount = job.getMaxRetryCount() == null ? 3 : job.getMaxRetryCount();

        if (retryCount + 1 < maxRetryCount) {
            importJobRepo.requeueStaleJob(
                    job.getId(),
                    "IMPORT_WORKER_TIMEOUT",
                    "Import worker heartbeat timeout, job requeued by watchdog",
                    now
            );

            log.warn("Requeued stale import job. jobId={}, retryCount={}/{}",
                    job.getJobId(), retryCount + 1, maxRetryCount);
        } else {
            importJobRepo.markStaleJobFailed(
                    job.getId(),
                    "IMPORT_WORKER_TIMEOUT",
                    "Import worker heartbeat timeout, max retry reached",
                    now
            );

            log.error("Marked stale import job failed. jobId={}, retryCount={}/{}",
                    job.getJobId(), retryCount + 1, maxRetryCount);
        }
    }*/

    private String watchdogId() {
        return "watchdog-" + ManagementFactory.getRuntimeMXBean().getName();
    }
}