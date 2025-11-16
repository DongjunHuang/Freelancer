package com.example.repos;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatasetRecordRepo extends MongoRepository<DatasetRecord, String>, DatasetRecordRepoCustom {
}
