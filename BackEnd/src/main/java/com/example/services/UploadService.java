package com.example.services;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import java.util.Optional;

import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.repos.DatasetRecordRepo;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadata.VersionControl;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.MetadataStatus;
import com.example.requests.DatasetReq;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private final DatasetMetadataRepo metadataRepo;
    private final DatasetRecordRepo recordRepo;

    // Step1-2: create and save the metadata
    public DatasetMetadata updateDatasetMetadata(String datasetName, Long userId) throws Exception {
        Optional<DatasetMetadata> meta = metadataRepo.findByUserIdAndDatasetName(userId, datasetName);
        if (!meta.isPresent()) {
            throw new Exception();
        }

        Instant now = Instant.now();
        meta.get().setUpdatedAt(now);    
        
        // TODO: update staged information
        return meta.get();
    }


    // Step1-1: create and save the metadata
    public DatasetMetadata createDatasetMetadata(DatasetReq req, Long userId) throws Exception {
        Optional<DatasetMetadata> meta = metadataRepo.findByUserIdAndDatasetName(userId, req.getDatasetName());
        if (meta.isPresent()) {
            logger.error("The dateset {} exists, can not be created twice.", req.getDatasetName());
            //TODO: add specific exception 
            throw new Exception();
        }

        Instant now = Instant.now();

        // Newly created metadata
        VersionControl current = VersionControl.builder()
                                    .version(0)
                                    .headers(null)
                                    .rowCount(0)
                                    .build();

        // Receive indexes
        DatasetMetadata newDataset = DatasetMetadata.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .datasetName(req.getDatasetName())
            .status(MetadataStatus.READY)
            .createdAt(now)
            .updatedAt(now)
            .current(current)
            .staged(null)
            .recordDateColumnName(req.getRecordDateColumnName())
            .build();
        return newDataset;
    }

    // step 2: update headers
    // TODO: key might be modified in the future
    private void updateHeaders(DatasetMetadata data, List<String> headers) {
        LinkedHashSet<String> mergedHeaders = null;
        if (data.getCurrent().getHeaders() == null) {
            mergedHeaders = new LinkedHashSet<>();
        } else {
            mergedHeaders = new LinkedHashSet<>(data.getCurrent().getHeaders());
        }
        mergedHeaders.addAll(headers);

        VersionControl staged = VersionControl.builder()
                                    .version(data.getCurrent() == null ? 1 : data.getCurrent().getVersion() + 1)
                                    .headers(new ArrayList<>(mergedHeaders))
                                    .build();
        
        data.setStaged(staged);
        data.setStatus(MetadataStatus.IMPORTING);
        data.setUpdatedAt(Instant.now());
        logger.info("Save data {} to the datasets", data);
        
        metadataRepo.save(data);
    }

    public long importingRecords(MultipartFile file, DatasetMetadata data) throws Exception {
        try (var parser = new CSVParser(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().withIgnoreEmptyLines())) {
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
            logger.info("Insert into data record with headers {}", headers);

            // Update headers and save the data into metadata
            updateHeaders(data, headers);
            
            // This is customized index that can shorten the time of range searching
            String dateRecordColumnName = data.getRecordDateColumnName();

            // batch of rows to import
            long count = 0;
            String batchId = UUID.randomUUID().toString();
            List<Map<String, Object>> batch_segment = new ArrayList<>();

            // go through all lines to put data to mongodb db by batches
            for (CSVRecord record : parser) {
                Map<String, Object> row = new HashMap<>();
                for (String header : parser.getHeaderNames()) {
                    row.put(header, record.get(header));
                }

                batch_segment.add(row);
                count++;
               
                if (count % 300 == 0) {
                    recordRepo.bulkInsertRecords(data, batch_segment, batchId, dateRecordColumnName);
                    batch_segment.clear();
                }
            }
            
            if (!batch_segment.isEmpty()) {
                recordRepo.bulkInsertRecords(data, batch_segment, batchId, dateRecordColumnName);
            }

            return count;
        } catch (Exception ex) {
            logger.info("Exception throwed out" + ex);
            throw new Exception();
        }
    }

    @Transactional
    public void promoteStagedToCurrent(String datasetName, Long userId, long importedRows) {
        Optional<DatasetMetadata> meta = metadataRepo.findByUserIdAndDatasetName(userId, datasetName);
        DatasetMetadata dataset = meta.get();
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
