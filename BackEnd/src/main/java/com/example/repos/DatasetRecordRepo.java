package com.example.repos;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasetRecordRepo extends MongoRepository<DatasetRecord, String>, DatasetRecordRepoCustom {
    @Query("""
        {
            'datasetId': ?0,
            'version': ?1,
            'recordDate': { '$gte': ?2, '$lte': ?3 }
        }
        """)
    public List<DatasetRecord> findByDatasetIdAndVersionAndUploadDateBetween(String datasetId, Integer version, LocalDate from, LocalDate to, Sort sort);
    
    @Query("""
    {
        'datasetId': ?0,
        'version': ?1,
        'recordDate': { '$gte': ?2, '$lte': ?3 },
        'symbol': { '$in': ?4 }
    }
    """)
    public List<DatasetRecord> findByDatasetIdAndVersionAndUploadDateBetweenAndSymbols(String datasetId, Integer version, LocalDate from, LocalDate to, List<String> symbols, Sort sort);
    
}