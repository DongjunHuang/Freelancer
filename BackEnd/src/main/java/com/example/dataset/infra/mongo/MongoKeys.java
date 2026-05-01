package com.example.dataset.infra.mongo;

public interface MongoKeys {
    interface Dataset {
        final String TABLE_NAME = "datasets";
        final String USERID = "userId";
        final String DATASET_NAME = "datasetName";
        final String STATUS = "status";
        final String STAGED = "staged";
        final String CURRENT = "current";
        final String ROW_COUNT = "rowCount";
        final String RECORD_COUNT = "rowCount";
        final String LAST_ERROR_CODE = "lastErrorCode";
        final String LAST_ERROR_MESSAGE = "lastErrorMessage";
        final String TIMEZONE = "timezone";
        final String CREATED_AT = "createAt";
        final String UPDATED_AT = "updatedAt";
        final String TIME_PATTERN = "datePattern";
        final String OBSOLETED = "obsoleted";
        final String OBSOLETED_AT = "obsoleted_at";

        // Record indexes
        final String RECORD_SYMBOL_NAME = "recordSymbolName";
        final String RECORD_DATE_COLUMN_NAME = "recordDateColumnName";
        final String RECORD_DATE_COLUMN_FORMAT = "recordDateColumnFormat";
    }

    interface Record {
        final String TABLE_NAME = "records";
        final String DATASET_ID = "datasetId";
        final String DATA = "data";
        final String VERSION = "version";
        final String BATCH_ID = "batchId";
        final String USER_DEFINED_TIME = "userDefinedTime";
        final String SYSTEM_CREATED_AT = "systemCreatedAt";
        final String INDEXED_VALUE = "indexedColumn";
    }
}
