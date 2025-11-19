package com.example.repos;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.example.repos.DatasetMetadata.VersionControl;
import com.mongodb.client.result.UpdateResult;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DatasetMetadataRepoImpl implements DatasetMetadataRepoCustom {
    private static final Logger logger = LoggerFactory.getLogger(DatasetMetadataRepoImpl.class);
    private final MongoTemplate mongo;

    @Override
    public void updateStagedHeaders(String datasetId, VersionControl staged, MetadataStatus status) {
        Query query = Query.query(Criteria.where(MongoKeys.Common.ID).is(datasetId));
        Update update = new Update()
                .set(MongoKeys.Dataset.STAGED, staged)
                .set(MongoKeys.Dataset.STATUS, status)
                .set(MongoKeys.Common.UPDATED_AT, Instant.now());
        UpdateResult result = mongo.updateFirst(query, update, DatasetMetadata.class);
        logger.info("[updateStagedHeaders] datasetId={}, matched={}, modified={}",
        datasetId, result.getMatchedCount(), result.getModifiedCount());
    }

    @Override
    public void promoteStagedToCurrent(String datasetId) {
        DatasetMetadata meta = mongo.findById(datasetId, DatasetMetadata.class);
        if (meta == null || meta.getStaged() == null) return;
        Query query = Query.query(Criteria.where(MongoKeys.Common.ID).is(datasetId));
        Update update = new Update()
                .set(MongoKeys.Dataset.CURRENT, meta.getStaged())
                .set(MongoKeys.Dataset.STAGED, null)
                .set(MongoKeys.Dataset.STATUS, MetadataStatus.READY)
                .set(MongoKeys.Common.UPDATED_AT, Instant.now());
        mongo.updateFirst(query, update, DatasetMetadata.class);
    }

    @Override
    public void discardStaged(String datasetId) {
        Query query = Query.query(Criteria.where(MongoKeys.Common.ID).is(datasetId));
        Update update = new Update()
                .set(MongoKeys.Dataset.STAGED, null)
                .set(MongoKeys.Dataset.STATUS, MetadataStatus.FAILED)
                .set(MongoKeys.Common.UPDATED_AT, Instant.now());
        mongo.updateFirst(query, update, DatasetMetadata.class);
    }
}