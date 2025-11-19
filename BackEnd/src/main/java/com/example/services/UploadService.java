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
        try (var parser = new CSVParser(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().withIgnoreEmptyLines())) {
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
            logger.info("Insert into data record with headers {}", headers);
            DatasetMetadata dataset = metadataRepo.findByUserIdAndDatasetName(dataRecordProps.getUserId(),
                                                        dataRecordProps.getDatasetName());
            if (dataset == null) {
                // Step1
                dataset = builder.createIfNotPresentDatasetMetadata(dataRecordProps, metadataRepo);
            }

            Set<String> columnsNeedsInfer = new HashSet<>();
            
            // Step2
            builder.mergeAndFillInferNeededColumns(columnsNeedsInfer, dataset, headers);

            dataRecordProps.setStagedVersion(dataset.getCurrent().getVersion() + 1);
            dataRecordProps.setDatasetId(dataset.getId());
            
            int batch_count = 1;
            Iterator<CSVRecord> it = parser.iterator();
            List<Map<String, String>> rows = new ArrayList<>();
            List<Map<String, String>> inferRows = new ArrayList<>();
                
            while (it.hasNext()) {
                CSVRecord record = it.next();
                Map<String, String> row = new HashMap<>();
                for (String header : parser.getHeaderNames()) {
                    row.put(header, record.get(header));
                }
                
                
                rows.add(row);
                if (batch_count < INFER_NUM) {
                    inferRows.add(row);
                } else if (batch_count == INFER_NUM) {
                    // Step3: save the metadata
                    builder.inferAndfillStagedColumns(dataset, inferRows, columnsNeedsInfer);
                    
                    // Step4: save the metadata
                    builder.saveDataset(dataset, metadataRepo);
                }
            
                if (batch_count % BATCH_NUM == 0 || !it.hasNext()) {
                    recordRepo.bulkInsertRecords(rows, dataRecordProps);
                    rows.clear();
                }
                
                batch_count++;
            }

            return batch_count;
        } catch (Exception ex) {
            logger.info("Exception throwed out" + ex);
            throw new Exception();
        }
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
