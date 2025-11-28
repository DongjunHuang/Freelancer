package com.example.repos;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(MongoKeys.Dataset.COLLECTION)
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@CompoundIndex(name = "user_dataset_idx", def = "{'userId': 1, 'datasetName': 1}", unique = true)
public class DatasetMetadata {
    // ==========================================
    @Id 
    private String id;         
  
    @Field(MongoKeys.Dataset.USERID)
    @Indexed
    private Long userId;     
     
    // The creation time of the table
    @Field(MongoKeys.Common.CREATED_AT)
    private Instant createdAt;

    // The last updated time
    @Field(MongoKeys.Common.UPDATED_AT)
    private Instant updatedAt;

    @Field(MongoKeys.Dataset.DATASET_NAME)
    private String datasetName;

    @Enumerated(EnumType.STRING)
    @Field(MongoKeys.Dataset.STATUS)
    private MetadataStatus status;           
    
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

    @Builder
    @Data
    public static class VersionControl {
        private Integer version;   
        private List<ColumnMeta> headers;
        private long rowCount;     
    }

    @Builder
    @Data
    public static class ColumnMeta {
        private String columnName;
        private ColumnType dataType;
        // TODO: change to column role
        private boolean metric;      
    }
}

