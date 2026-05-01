package com.example.dataset.infra.mongo;

import java.util.List;
import java.util.Map;

import com.example.dataset.domain.CreateCollectionDataProps;

public interface DatasetRecordRepoCustom {
    void bulkInsertRecords(List<Map<String, String>> rows, CreateCollectionDataProps dataRecordProps);
}
