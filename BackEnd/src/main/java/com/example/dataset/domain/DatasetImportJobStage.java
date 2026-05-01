package com.example.dataset.domain;

public enum DatasetImportJobStage {
    CREATED,      // job created
    UPLOADING,    // uploading files
    UPLOADED,     // file is uploaded and ready to be claimed
    IMPORTING,    // file is being red and imported to the database
    FINALIZING,   // switch the version
    CLEANING,     // clean the leftovers
    DONE,         // done
    ERROR         // error
}