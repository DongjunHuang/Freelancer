package com.example.async.importdataset;

public interface ImportProgressUpdater {
    void update(long processedRows, long successRows, long failedRows);
}