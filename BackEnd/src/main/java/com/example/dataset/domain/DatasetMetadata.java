package com.example.dataset.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.example.dataset.infra.mongo.MongoKeys;
import com.example.exception.ErrorCode;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(MongoKeys.Dataset.TABLE_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name = "user_dataset_active_idx",
        def = "{'userId': 1, 'datasetName': 1, 'obsoleted': 1}",
        unique = true
)
@CompoundIndex(
        name = "user_active_idx",
        def = "{'userId': 1, 'obsoleted': 1, 'updatedAt': -1}"
)
public class DatasetMetadata {
    @Id
    private String id;

    @Field(MongoKeys.Dataset.USERID)
    @Indexed
    private Long userId;

    // The creation time of the table
    @CreatedDate
    @Field(MongoKeys.Dataset.CREATED_AT)
    private Instant createdAt;

    // The last updated time
    @LastModifiedDate
    @Field(MongoKeys.Dataset.UPDATED_AT)
    private Instant updatedAt;

    @Field(MongoKeys.Dataset.DATASET_NAME)
    private String datasetName;

    @Enumerated(EnumType.STRING)
    @Field(MongoKeys.Dataset.STATUS)
    private DatasetStatus status;

    @Field(MongoKeys.Dataset.CURRENT)
    private VersionControl current;

    @Field(MongoKeys.Dataset.STAGED)
    private VersionControl staged;

    @Field(MongoKeys.Dataset.RECORD_DATE_COLUMN_NAME)
    private String recordDateColumnName;

    @Field(MongoKeys.Dataset.RECORD_DATE_COLUMN_FORMAT)
    private String recordDateColumnFormat;

    @Field(MongoKeys.Dataset.RECORD_SYMBOL_NAME)
    private String recordSymbolName;

    @Field(MongoKeys.Dataset.LAST_ERROR_CODE)
    private ErrorCode lastErrorCode;

    @Field(MongoKeys.Dataset.LAST_ERROR_MESSAGE)
    private String lastErrorMessage;

    @Field(MongoKeys.Dataset.TIMEZONE)
    private String timezone;

    @Field(MongoKeys.Dataset.TIME_PATTERN)
    private String timePattern;

    @Field(MongoKeys.Dataset.OBSOLETED)
    private Boolean obsoleted;

    @Field(MongoKeys.Dataset.OBSOLETED_AT)
    private Instant obsoletedAt;
}
