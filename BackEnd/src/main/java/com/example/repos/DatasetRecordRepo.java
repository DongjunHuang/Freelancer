package com.example.repos;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatasetRecordRepo extends MongoRepository<DatasetRecord, String>, DatasetRecordRepoCustom {
    public List<DatasetRecord> findByDatasetIdAndVersionAndUploadDateBetween(String datasetId, Integer version, LocalDate from, LocalDate to);
    public List<DatasetRecord> findByDatasetIdAndVersionAndRecordDateBetween();
}