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
        DatasetMetadata meta = datasetRepo.findByUserIdAndDatasetName(userId, req.getDatasetName());
        if (meta == null || meta.getCurrent() == null) {
            throw new IllegalArgumentException("Dataset not found or no current version");
        }

        String datasetId = meta.getId();
        Integer version = meta.getCurrent().getVersion();

        // TODO: add search by record date
        List<DatasetRecord> records = recordRepo.findByDatasetIdAndVersionAndUploadDateBetween(
                        datasetId,
                        version,
                        req.getFromDate(),
                        req.getToDate()
                );

        List<String> targetColumns;
        if (req.getColumns() == null || req.getColumns().isEmpty()) {
            targetColumns = meta.getCurrent().getHeaders().stream()
                    .map(ColumnMeta::getColumnName)
                    .toList();
        } else {
            targetColumns = req.getColumns();
        }

        List<DataRowDto> rows = new ArrayList<>();
        for (DatasetRecord r : records) {
            DataRowDto rowDto = new DataRowDto();
            rowDto.setUploadDate(r.getUploadDate());

            Map<String, Object> filtered = new LinkedHashMap<>();
            Map<String, Object> src = r.getData();
            for (String col : targetColumns) {
                filtered.put(col, src != null ? src.get(col) : null);
            }
            rowDto.setValues(filtered);
            rows.add(rowDto);
        }

        FetchRecordsResp resp = new FetchRecordsResp();
        resp.setDatasetName(req.getDatasetName());
        resp.setColumns(targetColumns);
        resp.setRows(rows);
        return resp;
    }
}
