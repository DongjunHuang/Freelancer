package com.example.services;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.repos.DatasetRecordRepo;
import com.example.models.DataProps;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.MetadataStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private final DatasetMetadataRepo metadataRepo;
    private final DatasetRecordRepo recordRepo;
    private final DatasetBuilder builder;

    private final static  int BATCH_NUM = 300;
    private final static  int INFER_NUM = 30;

    public long appendRecords(MultipartFile file, DataProps dataRecordProps) throws Exception {

        // ====== Phase 1：parse first N lines, infer data types and save metadata dataset ======
        DatasetMetadata dataset;
    
        try (var parser = new CSVParser(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withTrim()
                         .withIgnoreEmptyLines())) {
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
            logger.info("Append records, headers = {}", headers);
    
            dataset = builder.createIfNotPresentDatasetMetadata(dataRecordProps, metadataRepo);
            if (dataset == null) {
                // TODO: 
                throw new IllegalStateException("DatasetMetadata is null");
            }
    
            Set<String> columnsNeedsInfer = new HashSet<>();
            builder.mergeAndFillInferNeededColumns(columnsNeedsInfer, dataset, headers);
    
            List<Map<String, String>> inferRows = new ArrayList<>();
            Iterator<CSVRecord> it = parser.iterator();
            int inspected = 0;
    
            while (it.hasNext() && inspected < INFER_NUM) {
                CSVRecord record = it.next();
                Map<String, String> row = new HashMap<>();
                for (String header : headers) {
                    row.put(header, record.get(header));
                }
                inferRows.add(row);
                inspected++;
            }
    
          
            if (!inferRows.isEmpty() && !columnsNeedsInfer.isEmpty()) {
                builder.inferAndfillStagedColumns(dataset, inferRows, columnsNeedsInfer);
            }
    
           
    
            builder.saveDataset(dataset, metadataRepo);
            dataRecordProps.setStagedVersion(dataset.getCurrent().getVersion() + 1);
            dataRecordProps.setDatasetId(dataset.getId());
            
            logger.info("Dataset metadata saved. datasetId={}, stagedVersion={}",
                    dataRecordProps.getDatasetId(), dataRecordProps.getStagedVersion());
        }
    
      
         // ====== Phase 2：parse the whole file, put all data into records ======
        long totalInserted = 0;
    
        try (var parser = new CSVParser(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withTrim()
                         .withIgnoreEmptyLines()
        )) {
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
            Iterator<CSVRecord> it = parser.iterator();
    
            List<Map<String, String>> rows = new ArrayList<>();
            int batchCount = 0;
    
            while (it.hasNext()) {
                CSVRecord record = it.next();
                Map<String, String> row = new HashMap<>();
                for (String header : headers) {
                    String headerUppercase = header.toUpperCase();
                    row.put(headerUppercase, record.get(header));
                }
                rows.add(row);
                batchCount++;
    
                if (batchCount == BATCH_NUM) {
                    recordRepo.bulkInsertRecords(rows, dataRecordProps);
                    totalInserted += batchCount;
                    rows.clear();
                    batchCount = 0;
                }
            }
    
            if (!rows.isEmpty()) {
                recordRepo.bulkInsertRecords(rows, dataRecordProps);
                totalInserted += rows.size();
            }
        }
    
        return totalInserted;
    }

    @Transactional
    public void promoteStagedToCurrent(String datasetName, Long userId, long importedRows) throws Exception {
        DatasetMetadata dataset = metadataRepo.findByUserIdAndDatasetName(userId, datasetName);
        if (dataset == null) {
            // TODO: add exception type
            throw new Exception();
        }
        var staged = dataset.getStaged();
        var current = dataset.getCurrent();
        
        current.setVersion(staged.getVersion());
        current.setHeaders(new ArrayList<>(staged.getHeaders()));
        current.setRowCount(staged.getRowCount() + importedRows);

        dataset.setUpdatedAt(Instant.now());
        dataset.setStatus(MetadataStatus.READY);
        dataset.setStaged(null);

        metadataRepo.save(dataset);
    }

    // TODO: when user upload fails we should roll back the batch
    public void rollback(String batchId) {

    }
}
