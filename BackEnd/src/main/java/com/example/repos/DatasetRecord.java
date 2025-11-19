package com.example.repos;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(MongoKeys.Record.COLLECTION)
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@CompoundIndex(name = "idx_dataset_recordDate", def = "{'datasetId': 1, 'recordDate': 1}")
@CompoundIndex(name = "idx_dataset_uploadDate", def = "{'datasetId': 1, 'uploadDate': 1}")
@CompoundIndex(name = "idx_dataset_version_recordDate", def = "{ 'datasetId': 1, 'version': 1, 'recordDate': 1 }")
@CompoundIndex(name = "idx_dataset_version_uploadDate", def = "{ 'datasetId': 1, 'version': 1, 'uploadDate': 1 }")
public class DatasetRecord {   
    @Id
    private String id;                 
  
    @Indexed
    @Field(MongoKeys.Record.DATASET_ID)
    private String datasetId;
    
    @Field(MongoKeys.Record.DATA)
    private Map<String, Object> data;      
        
    @Field(MongoKeys.Record.VERSION)
    private Integer version;
    
    @Field(MongoKeys.Record.BATCH_ID)
    @Indexed
    private String batchId; 

    @Field(MongoKeys.Common.UPDATED_AT)
    private Instant updatedAt; 

    // The field used to record the time the data point uploaded
    @Field(MongoKeys.Record.UPLOAD_DATE)
    private LocalDate uploadDate; 

    // The field used to locate time of the datapoint
    @Field(MongoKeys.Record.RECORD_DATE)
    private LocalDate recordDate; 
}