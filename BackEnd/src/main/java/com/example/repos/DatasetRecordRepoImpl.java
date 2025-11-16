package com.example.repos;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

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

    // TODO: currently I only consider insert situation, if the user insert multiple same CSV files, we need to deduplicate the related records
    @Override
    public void bulkInsertRecords(DatasetMetadata data, List<Map<String, Object>> rows, String batchId, String recordDateColumnName) {
        MongoCollection<Document> coll = mongo.getCollection(MongoKeys.Common.RECORD_TABLENAME);
        List<WriteModel<Document>> batch = new ArrayList<>(rows.size());
        
        Instant now = Instant.now();
        LocalDate uploadedDate = LocalDate.now(ZoneOffset.UTC);

        for (Map<String, Object> row : rows) {
            DatasetRecord record = DatasetRecord.builder()
                .datasetId(data.getId())
                .version(data.getStaged().getVersion())
                .updatedAt(now)
                .uploadDate(uploadedDate)       
                .batchId(batchId)
                .build();
            
            // Ignore the column if not available
            if (recordDateColumnName != null && row.containsKey(recordDateColumnName)) {
                Object raw = row.get(recordDateColumnName);
                if (raw != null) {
                    LocalDate recordTime = dateParser.parseRecordTime(raw.toString());
                    record.setRecordDate(recordTime);
                }
            }

            Document doc = toDocument(record, row);
            batch.add(new InsertOneModel<>(doc));
        }
        if (!batch.isEmpty()) {
            coll.bulkWrite(batch, new BulkWriteOptions().ordered(false));
        }
    }

    private Document toDocument(DatasetRecord record, Map<String, Object> row) {
        Document doc = new Document()
                .append(MongoKeys.Record.DATASET_ID, record.getDatasetId())
                .append(MongoKeys.Record.VERSION, record.getVersion())
                .append(MongoKeys.Common.UPDATED_AT, record.getUpdatedAt())
                .append(MongoKeys.Record.UPLOAD_DATE, record.getUploadDate())
                .append(MongoKeys.Record.BATCH_ID, record.getBatchId())
                .append(MongoKeys.Record.RECORD_DATE, record.getRecordDate())
                .append(MongoKeys.Record.DATA, new Document(row));
        return doc;
    }
}