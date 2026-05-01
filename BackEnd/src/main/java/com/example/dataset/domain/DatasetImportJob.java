package com.example.dataset.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "dataset_import_job")
@Data
public class DatasetImportJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;    // Not null
    private Long fileSize;  // Not null
    private Long totalRows;
    private Long processedRows;
    private Long successRows;
    private Long failedRows;
    private Long retry;     // Not null

    private String jobId;   // Not null
    private String tempFilePath;
    private String originalFileName;    // Not null
    private String metadata;
    private String errorCode;
    private String datasetName;

    private Instant startedAt;
    private Instant completedAt;


    @Enumerated(EnumType.STRING)
    private DatasetImportJobStatus status;  // Not null

    @Enumerated(EnumType.STRING)
    private DatasetImportJobStage stage;    // Not null

    @Enumerated(EnumType.STRING)
    private FileStorageType fileStorageType;    // Not null

    @Enumerated(EnumType.STRING)
    private DatasetImportType type;

    private String errorMessage;

    private Instant createdAt;
    private Instant updatedAt;
}