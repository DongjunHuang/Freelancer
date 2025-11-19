package com.example.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.models.DataProps;
import com.example.repos.ColumnType;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadata.ColumnMeta;
import com.example.repos.DatasetMetadata.VersionControl;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.MetadataStatus;
import com.example.utils.ColumnsTypeInfer;

@Component
public class DatasetBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DatasetBuilder.class);

    public void inferAndfillStagedColumns(DatasetMetadata dataset, 
                                    List<Map<String, String>> inferRows, 
                                    Set<String> columnsNeedsInfer) {
        Map<String, List<String>> map = new HashMap<>();
        for (int i = 0; i < inferRows.size(); i++) {
            Map<String, String> oneRow = inferRows.get(i);
            for (String header : oneRow.keySet()) {
                if (columnsNeedsInfer.contains(header)) {
                    if (!map.containsKey(header)) {
                        map.put(header, new ArrayList<>());
                    }
                    map.get(header).add(oneRow.get(header));
                }            
            }
        }

        //infer and fill out the dataset
        for (int i = 0; i < dataset.getStaged().getHeaders().size(); i++) {
            ColumnMeta column = dataset.getStaged().getHeaders().get(i);
            String header = column.getColumnName();
            if (!columnsNeedsInfer.contains(header)) {
                continue;
            }

            // fill the column
            ColumnType type = ColumnsTypeInfer.inferColumnType(header, map.get(header));
            boolean isMetric = isMetricColumn(header, type);

            // update staged header
            column.setDataType(type);
            column.setMetric(isMetric);
        }
    }


    public void mergeAndFillInferNeededColumns(Set<String> columnsNeedsInfer, 
                                            DatasetMetadata dataset, 
                                            List<String> headers) throws Exception {
        VersionControl staged = VersionControl.builder()
            .headers(new ArrayList<>())
            .rowCount(dataset.getCurrent().getRowCount())
            .version(dataset.getCurrent().getVersion() + 1)
            .build();

        Set<String> currentColumns = new HashSet<>();
        
        // put to set for dedup
        for (int i = 0; i < dataset.getCurrent().getHeaders().size(); i++) {
            currentColumns.add(dataset.getCurrent().getHeaders().get(i).getColumnName());
            
            // put existed to the staged
            staged.getHeaders().add(dataset.getCurrent().getHeaders().get(i));
        }

        // merge and dedup
        for (int i = 0; i < headers.size(); i++) {
            if (!currentColumns.contains(headers.get(i))) {
                columnsNeedsInfer.add(headers.get(i));

                // By default is string, we need to infer next
                ColumnMeta column = ColumnMeta.builder()
                                        .columnName(headers.get(i))
                                        .dataType(ColumnType.STRING)
                                        .isMetric(false)
                                        .build();
                staged.getHeaders().add(column);
            }
        }
        dataset.setStaged(staged);
        logger.info("Successfully merged headers for user {} with dataset {}", dataset.getUserId(), dataset.getDatasetName());
    }

    // Create and update dataset metadata
    public DatasetMetadata createIfNotPresentDatasetMetadata(
                            DataProps props, 
                            DatasetMetadataRepo metadataRepo) throws Exception {
            Instant now = Instant.now();
            // Newly created metadata
            VersionControl current = VersionControl.builder()
                                        .version(0)
                                        .headers(new ArrayList<>())
                                        .rowCount(0)
                                        .build();

            // Receive indexes
            DatasetMetadata dataset = DatasetMetadata.builder()
                .id(UUID.randomUUID().toString())
                .userId(props.getUserId())
                .datasetName(props.getDatasetName())
                .status(MetadataStatus.READY)
                .createdAt(now)
                .updatedAt(now)
                .current(current)
                .staged(null)
                .recordDateColumnName(props.getRecordDateColumnName())
                .build();
        logger.info("Successfully find or created dataset for user {} with dataset {}", props.getUserId(), props.getDatasetName());
        return dataset;
    }

    // The final stage to save the dataset metadata information
    public void saveDataset(DatasetMetadata dataset, DatasetMetadataRepo metadataRepo) {
        metadataRepo.save(dataset);
    }

    private boolean isMetricColumn(String header, ColumnType type) {
        if (type != ColumnType.NUMBER) {
            return false;
        }
        if (header == null)  {
            return true; 
        }
        
        String h = header.trim().toLowerCase();
        if (h.contains("id") || h.contains("code") || h.contains("no")
            || h.contains("number") || h.contains("zip") || h.contains("postal")) {
            return false;
        }
    
        return true;
    }
}
