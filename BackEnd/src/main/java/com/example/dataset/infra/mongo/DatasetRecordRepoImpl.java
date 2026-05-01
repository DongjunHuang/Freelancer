package com.example.dataset.infra.mongo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.dataset.domain.CreateCollectionDataProps;
import com.example.dataset.domain.DatasetRecord;
import com.example.utils.DateParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DatasetRecordRepoImpl implements DatasetRecordRepoCustom {
    private static final Logger logger = LoggerFactory.getLogger(DatasetRecordRepoImpl.class);

    private final MongoTemplate mongo;
    private final DateParser dateParser;

    // TODO: currently I only consider insert situation, if the user insert multiple
    // same CSV files, we need to deduplicate the related records
    @Override
    public void bulkInsertRecords(List<Map<String, String>> rows, CreateCollectionDataProps dataProps) {
        MongoCollection<Document> coll = mongo.getCollection(MongoKeys.Record.TABLE_NAME);
        List<WriteModel<Document>> batch = new ArrayList<>(rows.size());

        Instant now = Instant.now();

        for (Map<String, String> row : rows) {
            Instant recordedTime = DateParser.parseRecordTime(row.get(dataProps.getRecordTimeColumnName()), dataProps.getRecordTimeColumnFormat(), dataProps.getTimezone());
            DatasetRecord record = DatasetRecord.builder()
                    .datasetId(dataProps.getDatasetId())
                    .version(dataProps.getVersion())
                    .systemCreatedAt(now)
                    .userDefinedTime(recordedTime)
                    .batchId(dataProps.getBatchId())
                    .indexedValue(row.get(dataProps.getRecordPrimaryIndexedColumnName()))
                    .build();

            // Insert the batch into the mongo db
            Document doc = toDocument(record, row);
            batch.add(new InsertOneModel<>(doc));
        }
        if (!batch.isEmpty()) {
            coll.bulkWrite(batch, new BulkWriteOptions().ordered(false));
            batch.clear();
        }
    }

    private Document toDocument(DatasetRecord record, Map<String, String> row) {
        Document doc = new Document()
                .append(MongoKeys.Record.DATASET_ID, record.getDatasetId())
                .append(MongoKeys.Record.VERSION, record.getVersion())
                .append(MongoKeys.Record.BATCH_ID, record.getBatchId())
                .append(MongoKeys.Record.USER_DEFINED_TIME, record.getUserDefinedTime())
                .append(MongoKeys.Record.SYSTEM_CREATED_AT, record.getSystemCreatedAt())
                .append(MongoKeys.Record.INDEXED_VALUE, record.getIndexedValue())
                .append(MongoKeys.Record.DATA, new Document(row));
        return doc;
    }
}