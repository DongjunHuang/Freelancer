package com.example.dataset.domain;

public enum DatasetImportJobStatus {
    PENDING,    // Waiting to be handled
    RUNNING,    // Handling
    COMPLETED,  // Completed handling
    FAILED, // Failed
    CLEANING,   // Cleaning
    CANCELED    // User cancelled
}