package com.example.dataset.infra.jpa;

import com.example.dataset.domain.DatasetImportJob;
import com.example.dataset.domain.DatasetImportJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import java.util.Optional;

public interface DatasetImportJobRepository extends JpaRepository<DatasetImportJob, Long> {
    Optional<DatasetImportJob> findByJobId(String jobId);

    boolean existsByUserIdAndDatasetNameAndStatusIn(Long userId, String datasetName, List<DatasetImportJobStatus> statuses);

    @Query(value = """
            SELECT *
            FROM dataset_import_job
            WHERE status = 'PENDING'
              AND stage = 'UPLOADED'
            ORDER BY created_at ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<DatasetImportJob> findPendingUploadedJobs(@Param("limit") int limit);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE dataset_import_job
            SET status = 'RUNNING',
                stage = 'IMPORTING',
                worker_id = :workerId,
                started_at = :startedAt,
                updated_at = :startedAt
            WHERE id = :id
              AND status = 'PENDING'
              AND stage = 'UPLOADED'
            """, nativeQuery = true)
    int claimJob(@Param("id") Long id, @Param("workerId") String workerId, @Param("startedAt") Instant startedAt);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE dataset_import_job
            SET status = 'SUCCEEDED',
                stage = 'DONE',
                completed_at = :completedAt,
                updated_at = :completedAt
            WHERE job_id = :jobId
              AND status = 'RUNNING'
            """, nativeQuery = true)
    int markSucceeded(@Param("jobId") String jobId, @Param("completedAt") Instant completedAt);

    @Modifying
    @Transactional
    @Query(value = """
              UPDATE dataset_import_job
              SET status = 'FAILED',
                  stage = 'ERROR',
                  retry = retry + 1,
                  last_error_code = :errorCode,
                  last_error_message = :errorMessage,
                  completed_at = :completedAt,
                  updated_at = :completedAt
              WHERE job_id = :jobId
            AND status = 'RUNNING'
            AND stage IN ('IMPORTING', 'FINALIZING')
            AND retry < 3
            """, nativeQuery = true)
    int markFailed(@Param("jobId") String jobId, @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage, @Param("completedAt") Instant completedAt);


    @Modifying
    @Transactional
    @Query(value = """
            UPDATE dataset_import_job
            SET processed_rows = :processedRows,
                success_rows = :successRows,
                failed_rows = :failedRows,
                updated_at = :updatedAt
            WHERE job_id = :jobId
              AND status = 'RUNNING'
            """, nativeQuery = true)
    int updateProgress(@Param("jobId") String jobId, @Param("processedRows") long processedRows, @Param("successRows") long successRows, @Param("failedRows") long failedRows, @Param("updatedAt") Instant updatedAt);

    // TODO ==============================================================================================================================
    @Modifying
    @Query(value = """
            UPDATE dataset_import_job
            SET heartbeat_at = :now,
                updated_at = :now
            WHERE job_id = :jobId
              AND status = 'RUNNING'
            """, nativeQuery = true)
    int heartbeat(@Param("jobId") String jobId, @Param("now") Instant now);

    @Query(value = """
            SELECT *
            FROM dataset_import_job
            WHERE status = 'RUNNING'
              AND heartbeat_at < :deadline
            ORDER BY heartbeat_at ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<DatasetImportJob> findStaleRunningJobs(@Param("deadline") Instant deadline, @Param("limit") int limit);

    @Modifying
    @Query(value = """
            UPDATE dataset_import_job
            SET worker_id = :watchdogId,
                updated_at = :now
            WHERE id = :id
              AND status = 'RUNNING'
              AND heartbeat_at < :deadline
            """, nativeQuery = true)
    int claimStaleJob(@Param("id") Long id, @Param("watchdogId") String watchdogId, @Param("deadline") Instant deadline, @Param("now") Instant now);

    @Modifying
    @Query(value = """
            UPDATE dataset_import_job
            SET status = 'PENDING',
                stage = 'UPLOADED',
                worker_id = NULL,
                started_at = NULL,
                heartbeat_at = NULL,
                retry_count = retry_count + 1,
                last_error_code = :errorCode,
                last_error_message = :errorMessage,
                updated_at = :now
            WHERE id = :id
              AND status = 'RUNNING'
            """, nativeQuery = true)
    int requeueStaleJob(@Param("id") Long id, @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage, @Param("now") Instant now);

    @Modifying
    @Query(value = """
            UPDATE dataset_import_job
            SET status = 'FAILED',
                stage = 'ERROR',
                worker_id = NULL,
                finished_at = :now,
                last_error_code = :errorCode,
                last_error_message = :errorMessage,
                updated_at = :now
            WHERE id = :id
              AND status = 'RUNNING'
            """, nativeQuery = true)
    int markStaleJobFailed(@Param("id") Long id, @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage, @Param("now") Instant now);
}