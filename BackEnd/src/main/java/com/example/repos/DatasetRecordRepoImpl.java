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

import com.example.exception.BadRequestException;
import com.example.exception.ErrorCode;
import com.example.models.DataProps;
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
    public void bulkInsertRecords(List<Map<String, String>> rows, DataProps dataProps) {
        MongoCollection<Document> coll = mongo.getCollection(MongoKeys.Common.RECORD_TABLENAME);
        List<WriteModel<Document>> batch = new ArrayList<>(rows.size());

        Instant now = Instant.now();
        LocalDate uploadedDate = LocalDate.now(ZoneOffset.UTC);
        logger.info("Dataset id is {}", dataProps.getDatasetId());

        for (Map<String, String> row : rows) {
            DatasetRecord record = DatasetRecord.builder()
                    .datasetId(dataProps.getDatasetId())
                    .version(dataProps.getStagedVersion())
                    .updatedAt(now)
                    .uploadDate(uploadedDate)
                    .batchId(dataProps.getBatchId())
                    .build();

            // There are two lines needed to be added outside of data object.
            // The first is date column, critical to track the data from time
            // The second is index defined by client, which is cutomized column to speed up
            // search speed
            if (dataProps.getRecordDateColumnName() == null || !row.containsKey(dataProps.getRecordDateColumnName())) {
                throw new BadRequestException(ErrorCode.NOT_VALID_DATE_COLUMN);
            }

            String localtime = row.get(dataProps.getRecordDateColumnName());

            LocalDate recordTime = dateParser.parseRecordTime(localtime, dataProps.getRecordDateColumnFormat());

            record.setRecordDate(recordTime);

            if (dataProps.getRecordSymbolColumnName() == null
                    || !row.containsKey(dataProps.getRecordSymbolColumnName())) {
                throw new BadRequestException(ErrorCode.NOT_VALID_SYMBOL_COLUMN);
            }

            String symbolColumn = row.get(dataProps.getRecordSymbolColumnName());
            record.setSymbol(symbolColumn);

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
                .append(MongoKeys.Common.UPDATED_AT, record.getUpdatedAt())
                .append(MongoKeys.Record.UPLOAD_DATE, record.getUploadDate())
                .append(MongoKeys.Record.BATCH_ID, record.getBatchId())
                .append(MongoKeys.Record.RECORD_DATE, record.getRecordDate())
                .append(MongoKeys.Record.SYMBOL, record.getSymbol())
                .append(MongoKeys.Record.DATA, new Document(row));
        return doc;
    }
}