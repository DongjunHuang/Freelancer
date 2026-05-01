package com.example.dataset.app;

import com.example.dataset.domain.*;
import com.example.dataset.domain.dto.CreateDatasetReq;
import com.example.dataset.domain.dto.CreateDatasetResp;
import com.example.dataset.infra.jpa.DatasetImportJobRepository;
import com.example.s3.S3Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DatasetImportJobService {
    private static final Logger logger = LoggerFactory.getLogger(DatasetImportJobService.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final DatasetImportJobRepository importJobRepo;
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final ObjectMapper objectMapper;

    public CreateDatasetResp createImportJob(MultipartFile file, CreateDatasetReq req, Long userId) {
        String jobId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "dataset.csv";

        DatasetImportJobMetadata metadata = DatasetImportJobMetadata.builder()
                .recordPrimaryIndexedColumnName(req.getRecordPrimaryIndexedColumnName().toUpperCase())
                .recordTimeColumnName(req.getRecordTimeColumnName().toUpperCase())
                .recordTimeColumnFormat(req.getRecordTimeColumnFormat())
                .timezone(req.getTimezone())
                .build();
        String metadataJson = null;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize dataset import job metadata", e);
        }


        createPlaceholderImportJob(
                userId,
                req.getDatasetName(),
                jobId,
                originalFilename,
                file.getSize(),
                metadataJson);

        try {
            StoredObjectInfo tempFile = saveCsvTempFileToS3(file, userId, req.getDatasetName(), jobId);
            DatasetImportJob job = markImportJobUploaded(jobId, tempFile);
            return CreateDatasetResp.builder()
                    .jobId(job.getJobId())
                    .datasetName(job.getDatasetName())
                    .status(job.getStatus().toString())
                    .build();
        } catch (RuntimeException e) {
            markImportJobUploadFailed(jobId, e);
            throw e;
        }
    }

    @Transactional
    private void createPlaceholderImportJob(
            Long userId,
            String datasetName,
            String jobId,
            String originalFilename,
            long fileSize,
            String metadataJson) {
        boolean hasRunningJob = importJobRepo.existsByUserIdAndDatasetNameAndStatusIn(
                userId,
                datasetName,
                List.of(DatasetImportJobStatus.PENDING,
                        DatasetImportJobStatus.RUNNING)
        );

        if (hasRunningJob) {
            //TODO: add exception check here
            throw new IllegalStateException("Dataset already has a running import job");
        }
        Instant now = Instant.now();
        DatasetImportJob job = new DatasetImportJob();
        job.setJobId(jobId);
        job.setUserId(userId);
        job.setDatasetName(datasetName);
        job.setStatus(DatasetImportJobStatus.PENDING);
        job.setStage(DatasetImportJobStage.UPLOADING);
        job.setFileStorageType(FileStorageType.S3);
        job.setTempFilePath("");
        job.setOriginalFileName(originalFilename);
        job.setFileSize(fileSize);
        job.setMetadata(metadataJson);
        job.setType(DatasetImportType.CREATE_DATASET);
        job.setRetry(0L);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        importJobRepo.save(job);
    }

    @Transactional
    protected DatasetImportJob markImportJobUploaded(String jobId, StoredObjectInfo tempFile) {
        DatasetImportJob job = importJobRepo.findByJobId(jobId)
                .orElseThrow(() -> new IllegalStateException("Dataset import job not found: " + jobId));
        Instant now = Instant.now();
        job.setTempFilePath(tempFile.getObjectKey());
        job.setOriginalFileName(tempFile.getOriginalFilename());
        job.setFileSize(tempFile.getFileSize());
        job.setStage(DatasetImportJobStage.UPLOADED);
        job.setUpdatedAt(now);
        return importJobRepo.save(job);
    }

    @Transactional
    protected void markImportJobUploadFailed(String jobId, RuntimeException ex) {
        importJobRepo.findByJobId(jobId).ifPresent(job -> {
            job.setStatus(DatasetImportJobStatus.FAILED);
            job.setStage(DatasetImportJobStage.ERROR);
            job.setErrorMessage(truncateErrorMessage(ex.getMessage()));
            job.setUpdatedAt(Instant.now());
            importJobRepo.save(job);
        });
    }

    private String truncateErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    /**
     *
     * @param file
     * @param userId
     * @param datasetName
     * @param jobId
     * @return
     */
    private StoredObjectInfo saveCsvTempFileToS3(MultipartFile file, Long userId, String datasetName, String jobId) {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "dataset.csv";
        String objectKey = buildImportJobObjectKey(userId, datasetName, jobId, originalFilename);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(objectKey)
                .contentType(resolveContentType(file))
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read csv file for S3 upload", e);
        } catch (S3Exception e) {
            logger.error("Failed to upload csv temp file to S3. userId={}, datasetName={}, jobId={}, bucket={}, key={}",
                    userId, datasetName, jobId, s3Properties.getBucket(), objectKey, e);
            throw e;
        }

        return StoredObjectInfo.builder()
                .bucket(s3Properties.getBucket())
                .objectKey(objectKey)
                .originalFilename(originalFilename)
                .fileSize(file.getSize())
                .contentType(resolveContentType(file))
                .build();
    }

    private String buildImportJobObjectKey(Long userId, String datasetName, String jobId, String originalFilename) {
        String safeFilename = sanitizeFilename(originalFilename);
        return String.format("csv-import/temp/%d/%s/%s/%s",
                userId,
                datasetName,
                jobId,
                safeFilename);
    }

    private String sanitizeFilename(String value) {
        if (value == null || value.isBlank()) {
            return "dataset.csv";
        }
        return value.trim()
                .replace("\\", "/")
                .replaceAll(".*/", "")
                .replaceAll("[^A-Za-z0-9._-]", "_");
    }


    private String resolveContentType(MultipartFile file) {
        return file.getContentType() != null ? file.getContentType() : "text/csv";
    }
}
