package com.example.repos;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.example.requests.DataPoint;

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
@CompoundIndex(name = "idx_dataset_version_recordDate_symbol", def = "{ 'datasetId': 1, 'version': 1, 'recordDate': 1, symbol': 1 }")
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

    @Field(MongoKeys.Record.SYMBOL)
    private String symbol; 

    public static DataPoint toDataPoint(DatasetRecord record, List<String> columns) {
        DataPoint p = new DataPoint();

        Map<String, Object> values = new LinkedHashMap<>();
        Map<String, Object> raw = record.getData();

        for (String col : columns) {
            values.put(col, raw != null ? raw.get(col) : null);
        }

        p.setRecordDate(record.getRecordDate());
        p.setValues(values);
        return p;
    }
}