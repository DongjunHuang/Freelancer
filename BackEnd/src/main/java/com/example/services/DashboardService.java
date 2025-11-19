package com.example.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadata.ColumnMeta;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetRecord;
import com.example.repos.DatasetRecordRepo;
import com.example.requests.DataRowDto;
import com.example.requests.FetchRecordsReq;
import com.example.requests.FetchRecordsResp;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DatasetMetadataRepo datasetRepo;
    private final DatasetRecordRepo recordRepo;
    
    public List<DatasetMetadata> getUserDatasets(Long userId) {
        return  datasetRepo.findByUserId(userId);
    }

    public FetchRecordsResp fetchRecords(Long userId, FetchRecordsReq req) {
        // 1. 找到这个用户的 dataset metadata
        DatasetMetadata meta = datasetRepo.findByUserIdAndDatasetName(userId, req.getDatasetName());
        if (meta == null || meta.getCurrent() == null) {
            throw new IllegalArgumentException("Dataset not found or no current version");
        }

        String datasetId = meta.getId();
        Integer version = meta.getCurrent().getVersion();

        List<DatasetRecord> records = null; /*recordRepo.findByDatasetIdAndVersionAndRecordDateBetween(
                        datasetId,
                        version,
                        req.getFromDate(),
                        req.getToDate()
                );*/

        // 3. 确定要返回哪些列：如果 req.columns 为空，就用 metadata.current.headers
        List<String> targetColumns;
        if (req.getColumns() == null || req.getColumns().isEmpty()) {
            targetColumns = meta.getCurrent().getHeaders().stream()
                    .map(ColumnMeta::getColumnName)
                    .toList();
        } else {
            targetColumns = req.getColumns();
        }

        // 4. 组装 rows，按目标 columns 过滤数据
        List<DataRowDto> rows = new ArrayList<>();
        for (DatasetRecord r : records) {
            DataRowDto rowDto = new DataRowDto();
            // rowDto.setRecordDate(r.getRecordDate());

            Map<String, Object> filtered = new LinkedHashMap<>();
            Map<String, Object> src = r.getData();
            for (String col : targetColumns) {
                filtered.put(col, src != null ? src.get(col) : null);
            }
            // rowDto.setValues(filtered);

            rows.add(rowDto);
        }

        // 5. 组装响应
        FetchRecordsResp resp = new FetchRecordsResp();
        // resp.setDatasetName(req.getDatasetName());
        // resp.setColumns(targetColumns);
        // resp.setRows(rows);
        return resp;
    }
}
