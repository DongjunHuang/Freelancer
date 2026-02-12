package com.example.common.dataset.infra.mongo;

import java.util.List;
import java.util.Map;

import com.example.common.dataset.domain.DataProps;

public interface DatasetRecordRepoCustom {
    public void bulkInsertRecords(List<Map<String, String>> rows, DataProps dataRecordProps);
}
