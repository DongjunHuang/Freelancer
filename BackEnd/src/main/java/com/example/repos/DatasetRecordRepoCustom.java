package com.example.repos;

import java.util.List;
import java.util.Map;

public interface DatasetRecordRepoCustom {
    public void bulkInsertRecords(DatasetMetadata data, List<Map<String, Object>> rows, String batchId, String timeColumn);
}
