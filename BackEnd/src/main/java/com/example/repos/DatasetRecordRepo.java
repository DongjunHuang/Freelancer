package com.example.repos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasetRecordRepo extends MongoRepository<DatasetRecord, String>, DatasetRecordRepoCustom {
        /**
         * Find the datapoints within the date range.
         * 
         * @param datasetId the dataset id.
         * @param version   the version of the datapoint.
         * @param from      the start of the date.
         * @param to        the end of the date.
         * @param sort      sorting strategy.
         * 
         * @return the list of found datapoints.
         */
        @Query("""
                        {
                            'datasetId': ?0,
                            'version': { '$lte': ?1 },
                            'recordDate': { '$gte': ?2, '$lte': ?3 }
                        }
                        """)
        public List<DatasetRecord> findByDatasetIdAndVersionLteAndRecordDateBetween(String datasetId, Integer version,
                        LocalDate from, LocalDate to, Sort sort);

        /**
         * Find the datapoints within the date range and symbols passed.
         * 
         * @param datasetId the dateset id.
         * @param version   the version.
         * @param from      the start of the date.
         * @param to        the end of the date.
         * @param symbols   symbols to filter.
         * @param sort      sorting strategy.
         * 
         * @return the list of found datapoints.
         */
        @Query("""
                        {
                            'datasetId': ?0,
                            'version': { '$lte': ?1 },
                            'recordDate': { '$gte': ?2, '$lte': ?3 },
                            'symbol': { '$in': ?4 }
                        }
                        """)
        public List<DatasetRecord> findByDatasetIdAndVersionLteAndRecordDateBetweenAndSymbols(String datasetId,
                        Integer version, LocalDate from, LocalDate to, List<String> symbols, Sort sort);

        /**
         * delete records for the corresponding datasetid.
         * 
         * @param datasetId the dataset id.
         * 
         * @return the deleted row number.
         */
        long deleteByDatasetId(String datasetId);
}