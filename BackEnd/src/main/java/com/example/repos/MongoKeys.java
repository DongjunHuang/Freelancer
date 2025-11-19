package com.example.repos;

public interface MongoKeys {
    interface Common {
        final String ID = "_id";
        final String METADATA_TABLENAME = "metadata";
        final String RECORD_TABLENAME = "records";
        final String UPDATED_AT = "updatedAt";
        final String CREATED_AT = "createdAt";
    }
    
    interface Dataset {
        final String COLLECTION = "datesets";
        final String USERNAME = "username";
        final String USERID = "userId";
        final String DATASET_ID = "datasetId";
        final String DATASET_NAME = "datasetName";
        final String STATUS = "status";
        final String STAGED = "staged";
        final String CURRENT = "current";
        final String ROW_COUNT = "rowCount";
        final String RECORD_DATE_COLUMN_NAME = "recordDateColumnName";
        final String RECORD_DATE_COLUMN_FORMAT = "recordDateColumnFormat";
    }

    interface Record {
        final String COLLECTION = "records";
        final String DATASET_ID = "datasetId";
        final String ROW_ID = "rowId";
        final String DATA = "data";
        final String DATASET_NAME = "datasetName";
        final String VERSION = "version";
        final String INDEX_KEY = "indexKey";
        final String BATCH_ID = "batchId";
        final String UPLOAD_DATE = "uploadDate";
        final String RECORD_DATE = "recordDate";
    }
}