package com.example.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // The symbols that defined the metric is not metric with headers
    private static final String[] NON_METRIC_COLUMN_SYMBOLS = { "id", "code", "no", "number", "zip", "postal", "date" };

    /**
     * We will pick up several samples of selected columns to infer
     * 
     * @param dataset
     * @param inferRows
     * @param columnsNeedsInfer
     */
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

        // infer and fill out the dataset
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
        logger.info("Filled inferred columns for dataset name {}", dataset.getDatasetName());
    }

    /**
     * 1. Sometimes, user inputs different columns with the same data set name, to
     * keep the flexibility, we keep the original
     * columns and add new columns user adds.
     * 
     * 2. We pass the columns needed to be infered the types of the columns.
     * 
     * @param columnsNeedsInfer the columns user wants to infer, which is newly
     *                          added columns.
     * @param dataset           the dataset.
     * @param headers           the headers.
     */
    public void mergeAndFillInferNeededColumns(Set<String> columnsNeedsInfer,
            DatasetMetadata dataset,
            List<String> headers) {
        VersionControl staged = VersionControl.builder()
                .headers(new ArrayList<>())
                .rowCount(dataset.getCurrent().getRowCount())
                .version(dataset.getCurrent().getVersion() + 1)
                .build();

        Set<String> currentColumns = new HashSet<>();

        // put to set for dedup
        for (int i = 0; i < dataset.getCurrent().getHeaders().size(); i++) {
            ColumnMeta header = dataset.getCurrent().getHeaders().get(i);
            currentColumns.add(header.getColumnName());

            // put existed to the staged
            staged.getHeaders().add(header);
        }

        // merge and dedup
        for (int i = 0; i < headers.size(); i++) {
            if (!currentColumns.contains(headers.get(i))) {
                columnsNeedsInfer.add(headers.get(i));

                // By default is string, we need to infer next
                ColumnMeta column = ColumnMeta.builder()
                        .columnName(headers.get(i))
                        .dataType(ColumnType.STRING)
                        .metric(false)
                        .build();
                staged.getHeaders().add(column);
                currentColumns.add(headers.get(i));
            }
        }
        dataset.setStaged(staged);
        logger.info("Merged headers for datasetname {}", dataset.getDatasetName());
    }

    /**
     * Create dataset metadata if not presented.
     * 
     * @param props        the props passed to the function.
     * @param metadataRepo the repo
     * @return
     * @throws Exception
     */
    public DatasetMetadata createIfNotPresentDatasetMetadata(
            DataProps props,
            DatasetMetadataRepo metadataRepo) {
        if (!props.isNewDataset()) {
            DatasetMetadata dataset = metadataRepo.findByUserIdAndDatasetName(props.getUserId(),
                    props.getDatasetName());
            if (dataset != null) {
                props.setRecordDateColumnName(dataset.getRecordDateColumnName());
                props.setRecordSymbolColumnName(dataset.getRecordSymbolName());
                props.setDatasetId(dataset.getId());
            }
            return dataset;
        }

        Instant now = Instant.now();
        // Newly created metadata
        VersionControl current = VersionControl.builder()
                .version(0)
                .headers(new ArrayList<>())
                .rowCount(0L)
                .build();

        // Receive indexes
        DatasetMetadata dataset = DatasetMetadata.builder()
                .userId(props.getUserId())
                .datasetName(props.getDatasetName())
                .status(MetadataStatus.READY)
                .createdAt(now)
                .updatedAt(now)
                .current(current)
                .staged(null)
                .recordDateColumnName(props.getRecordDateColumnName())
                .recordSymbolName(props.getRecordSymbolColumnName())
                .build();
        logger.info("Find or created dataset for datasetname {}", props.getDatasetName());
        return dataset;
    }

    /**
     * Save the dataset information into the mongodb.
     * 
     * @param dataset      dataset.
     * @param metadataRepo repo.
     */
    public void saveDataset(DatasetMetadata dataset, DatasetMetadataRepo metadataRepo) {
        metadataRepo.save(dataset);
        logger.info("Saved meta dataset for dataset name {}", dataset.getDatasetName());
    }

    /**
     * Helper function, to check if current number header represents metric columns
     * because we infer
     * some number columns maybe not real numbers, for example
     * for zip code, phone number.
     * 
     * @param header the original header name.
     * @param type   the type of the column.
     * @return whether the column is metric.
     */
    public boolean isMetricColumn(String header, ColumnType type) {
        // If the type if not number, then should not be metric
        if (type != ColumnType.NUMBER || header == null) {
            return false;
        }

        String h = header.trim().toLowerCase();
        for (String symbol : NON_METRIC_COLUMN_SYMBOLS) {
            if (h.contains(symbol)) {
                return false;
            }
        }

        return true;
    }
}
