package com.example.dataset.infra.mongo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.dataset.domain.DatasetRecord;

public interface DatasetRecordRepo extends MongoRepository<DatasetRecord, String>, DatasetRecordRepoCustom {
    /**
     * Find records within the user-defined time range.
     *
     * @param datasetId the dataset id.
     * @param version   the current visible version.
     * @param from      inclusive start time.
     * @param to        exclusive end time.
     * @param sort      sorting strategy.
     * @return the list of found records.
     */
    @Query("""
            {
                'datasetId': ?0,
                'version': { '$lte': ?1 },
                'userDefinedTime': { '$gte': ?2, '$lt': ?3 }
            }
            """)
    List<DatasetRecord> findByDatasetIdAndVersionLteAndUserDefinedTimeRange(
            String datasetId,
            Integer version,
            Instant from,
            Instant to,
            Sort sort);

    /**
     * Find records within the user-defined time range and indexed values.
     *
     * @param datasetId     the dataset id.
     * @param version       the current visible version.
     * @param from          inclusive start time.
     * @param to            exclusive end time.
     * @param indexedValues indexed values to filter, e.g. symbols.
     * @param sort          sorting strategy.
     * @return the list of found records.
     */

    @Query("""
            {
                'datasetId': ?0,
                'version': { '$lte': ?1 },
                'userDefinedTime': { '$gte': ?2, '$lt': ?3 },
                'indexedColumn': { '$in': ?4 }
            }
            """)
    List<DatasetRecord> findByDatasetIdAndVersionLteAndUserDefinedTimeRangeAndIndexedValues(
            String datasetId,
            Integer version,
            Instant from,
            Instant to,
            List<String> indexedValues,
            Sort sort);

    /**
     * delete records for the corresponding datasetid.
     *
     * @param datasetId the dataset id.
     * @return the deleted row number.
     */
    long deleteByDatasetId(String datasetId);

    long deleteByDatasetIdAndVersion(Long datasetId, Integer version);
}