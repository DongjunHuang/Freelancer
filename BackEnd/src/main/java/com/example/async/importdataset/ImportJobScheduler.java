package com.example.async.importdataset;

import com.example.dataset.domain.DatasetImportJob;
import com.example.dataset.infra.jpa.DatasetImportJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Semaphore;

@Component
@RequiredArgsConstructor
public class ImportJobScheduler {
    private final static int PERMITS_NUMBER = 3;
    private final DatasetImportJobRepository importJobRepo;
    private final DatasetImportWorker importJobWorker;

    @Qualifier("importJobExecutor")
    private final ThreadPoolTaskExecutor importJobExecutor;

    private final Semaphore permits = new Semaphore(PERMITS_NUMBER);

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        int available = permits.availablePermits();
        if (available <= 0) {
            return;
        }

        // Find reserved jobs from the database
        List<DatasetImportJob> candidates = importJobRepo.findPendingUploadedJobs(available);
        String workerId = currentWorkerId();

        for (DatasetImportJob job : candidates) {
            permits.acquireUninterruptibly();
            try {
                int claimed = importJobRepo.claimJob(job.getId(), workerId, Instant.now());
                if (claimed != 1) {
                    permits.release();
                    continue;
                }
                importJobExecutor.execute(() -> {
                    try {
                        importJobWorker.process(job.getJobId());
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        permits.release();
                    }
                });
            } catch (Exception e) {
                permits.release();
                throw e;
            }
        }
    }

    private String currentWorkerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" +
                    ManagementFactory.getRuntimeMXBean().getName();
        } catch (Exception e) {
            return "unknown-worker";
        }
    }
}