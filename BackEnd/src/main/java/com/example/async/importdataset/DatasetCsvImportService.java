package com.example.async.importdataset;

import com.example.dataset.domain.*;
import com.example.dataset.infra.mongo.DatasetMetadataRepo;
import com.example.dataset.infra.mongo.DatasetRecordRepo;
import com.example.exception.ErrorCode;
import com.example.exception.types.NotFoundException;
import com.example.s3.S3Properties;
import com.example.utils.ColumnsTypeInfer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DatasetCsvImportService {
    private static final int BATCH_SIZE = 300;
    private final static int INFER_NUM = 30;

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final DatasetRecordRepo datasetRecordRepo;
    private final DatasetMetadataRepo datasetRepo;
    private final ObjectMapper objectMapper;

    public void createDatasetFromS3(DatasetImportJob job, ImportProgressUpdater progressUpdater) throws Exception {
        if (s3Properties.getBucket() == null || s3Properties.getBucket().isBlank()) {
            throw new IllegalStateException("S3 bucket is not configured");
        }

        if (job.getTempFilePath() == null || job.getTempFilePath().isBlank()) {
            throw new IllegalStateException("Job tempFilePath is empty");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(job.getTempFilePath())
                .build();

        long processedRows = 0;
        long successRows = 0;
        long failedRows = 0;

        CreateCollectionDataProps props = prepareDataPropsFromJob(job);

        // Phase 1: create collection for the metadata
        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            DatasetMetadata dataset = createMetadataCollection(parser, props);
            props.setDatasetId(dataset.getId());
            props.setVersion(dataset.getStaged().getVersion());
        }

        ImportResult result;

        // Phase 2: import records for the collection
        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            result = insertRecords(parser, props, progressUpdater);
        }
        if (result != null) {
            progressUpdater.update(result.getProcessedRows(), result.getSuccessRows(), result.getFailedRows());

            // Phase 3: commit the change
            commit(job.getDatasetName(), job.getUserId(), result);
        }
    }

    private CreateCollectionDataProps prepareDataPropsFromJob(DatasetImportJob job) {
        DatasetImportJobMetadata metadata;
        try {
            metadata = objectMapper.readValue(job.getMetadata(), DatasetImportJobMetadata.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize dataset import job metadata", e);
        }

        CreateCollectionDataProps props = CreateCollectionDataProps.builder()
                .recordTimeColumnFormat(metadata.getRecordTimeColumnFormat())
                .recordPrimaryIndexedColumnName(metadata.getRecordPrimaryIndexedColumnName())
                .recordTimeColumnFormat(metadata.getRecordTimeColumnFormat())
                .timezone(metadata.getTimezone())
                .datasetName(job.getDatasetName())
                .userId(job.getUserId())
                .batchId(UUID.randomUUID().toString())
                .build();
        return props;
    }

    public ImportResult insertRecords(CSVParser parser, CreateCollectionDataProps dataProps, ImportProgressUpdater updater) {
        long processedRows = 0;
        long successRows = 0;
        long failedRows = 0;
        List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
        List<Map<String, String>> rows = new ArrayList<>();

        Iterator<CSVRecord> it = parser.iterator();
        while (it.hasNext()) {
            CSVRecord record = it.next();
            Map<String, String> row = new HashMap<>();
            for (String header : headers) {
                row.put(header.toUpperCase(), record.get(header));
            }
            rows.add(row);
            processedRows++;

            if (rows.size() == BATCH_SIZE) {

                try {
                    datasetRecordRepo.bulkInsertRecords(rows, dataProps);
                    successRows += rows.size();
                } catch (Exception e) {
                    failedRows += rows.size();
                } finally {
                    rows.clear();
                    updater.update(processedRows, successRows, failedRows);
                }
            }
        }

        if (!rows.isEmpty()) {
            try {
                datasetRecordRepo.bulkInsertRecords(rows, dataProps);
                successRows += rows.size();
            } catch (Exception e) {
                failedRows += rows.size();
            } finally {
                rows.clear();
                updater.update(processedRows, successRows, failedRows);
            }
        }

        return ImportResult.builder()
                .failedRows(failedRows)
                .successRows(successRows)
                .processedRows(processedRows)
                .build();
    }

    public DatasetMetadata createMetadataCollection(CSVParser parser, CreateCollectionDataProps dataProps) {
        Optional<DatasetMetadata> datasetCollection = datasetRepo.findByUserIdAndDatasetName(dataProps.getUserId(),
                dataProps.getDatasetName());
        if (datasetCollection.isPresent()) {
            throw new IllegalStateException("Dataset already created");
        }
        List<String> headers = null;
        List<Map<String, String>> inferRows = new ArrayList<>();

        // Gather information for metadata collections
        headers = new ArrayList<>(parser.getHeaderMap().keySet());
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


        // Step 1 : prepare current version and staged version
        VersionControl stagedVersion = VersionControl.builder()
                .headers(new ArrayList<>())
                .rowCount(0L)
                .version(1)
                .build();

        VersionControl currentVersion = VersionControl.builder()
                .version(0)
                .headers(new ArrayList<>())
                .rowCount(0L)
                .build();

        // Step 2 : create dataset， version not created yet
        DatasetMetadata dataset = DatasetMetadata.builder()
                .userId(dataProps.getUserId())
                .datasetName(dataProps.getDatasetName())
                .status(DatasetStatus.UPLOADING)
                .staged(stagedVersion)
                .current(currentVersion)
                .timezone(dataProps.getTimezone())
                .recordDateColumnName(dataProps.getRecordTimeColumnName())
                .recordSymbolName(dataProps.getRecordPrimaryIndexedColumnName())
                .obsoleted(false)
                .build();

        // Step 3 : prepare headers
        Map<String, List<String>> map = new HashMap<>();
        for (int i = 0; i < inferRows.size(); i++) {
            Map<String, String> oneRow = inferRows.get(i);
            for (String header : oneRow.keySet()) {
                if (!map.containsKey(header)) {
                    map.put(header, new ArrayList<>());
                }
                map.get(header).add(oneRow.get(header));
            }
        }

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);

            // fill the column
            ColumnType type = ColumnsTypeInfer.inferColumnType(header, map.get(header));
            boolean isMetric = ColumnsTypeInfer.isMetricColumn(header, type);

            ColumnMeta column = ColumnMeta.builder()
                    .columnName(headers.get(i))
                    .dataType(type)
                    .metric(isMetric)
                    .build();
            dataset.getStaged().getHeaders().add(column);
        }

        datasetRepo.save(dataset);
        return dataset;
    }

    @Transactional
    public void commit(String datasetName, Long userId, ImportResult result) {
        DatasetMetadata dataset = datasetRepo.findByUserIdAndDatasetName(userId, datasetName)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATASET_NOT_FOUND));
        var staged = dataset.getStaged();
        var current = dataset.getCurrent();
        current.setVersion(staged.getVersion());
        current.setHeaders(new ArrayList<>(staged.getHeaders()));
        current.setRowCount(staged.getRowCount() + result.getSuccessRows());
        dataset.setStatus(DatasetStatus.ACTIVE);
        dataset.setStaged(null);
        datasetRepo.save(dataset);
    }
}
