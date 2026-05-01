package com.example.dataset.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.example.dataset.infra.mongo.MongoKeys;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(MongoKeys.Record.TABLE_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
        @CompoundIndex(
                name = "idx_dataset_version_user_time",
                def = "{ 'datasetId': 1, 'version': 1, 'userDefinedTime': 1 }"
        ),
        @CompoundIndex(
                name = "idx_dataset_version_indexed_column_user_time",
                def = "{ 'datasetId': 1, 'version': 1, 'indexedValue': 1, 'userDefinedTime': 1 }"
        )
})
public class DatasetRecord {
    @Id
    private String id;

    @Field(MongoKeys.Record.DATASET_ID)
    private String datasetId;

    @Field(MongoKeys.Record.DATA)
    private Map<String, String> data;

    @Field(MongoKeys.Record.VERSION)
    private Integer version;

    @Field(MongoKeys.Record.BATCH_ID)
    private String batchId;

    @Field(MongoKeys.Record.USER_DEFINED_TIME)
    private Instant userDefinedTime;

    @Field(MongoKeys.Record.SYSTEM_CREATED_AT)
    private Instant systemCreatedAt;

    @Field(MongoKeys.Record.INDEXED_VALUE)
    private String indexedValue;
}