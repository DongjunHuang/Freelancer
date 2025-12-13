package com.example.repos;

import java.util.List;
import java.util.Map;

import com.example.models.DataProps;

public interface DatasetRecordRepoCustom {
    public void bulkInsertRecords(List<Map<String, String>> rows, DataProps dataRecordProps);
}
