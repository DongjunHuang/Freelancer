package com.example.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.exception.BadRequestException;
import com.example.exception.ErrorCode;
import com.example.guards.DatasetAction;
import com.example.guards.DatasetStateGuard;
import com.example.models.DataProps;
import com.example.repos.ColumnType;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadata.ColumnMeta;
import com.example.repos.DatasetMetadata.VersionControl;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetStatus;
import com.example.utils.ColumnsTypeInfer;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Builder
public class DatasetBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DatasetBuilder.class);

    private final DatasetStateGuard datasetGuard;

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
            boolean isMetric = ColumnsTypeInfer.isMetricColumn(header, type);

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
     */
    public DatasetMetadata createIfNotPresentDatasetMetadata(
            DataProps props,
            DatasetMetadataRepo metadataRepo) {
        Optional<DatasetMetadata> datasetOptional = metadataRepo.findByUserIdAndDatasetName(props.getUserId(),
                props.getDatasetName());

        // If the dataset is existed.
        if (datasetOptional.isPresent()) {
            // If the user requests to create a new dataset, we should not allow for the
            // same dataset name.
            // Otherwise, we only need to update the dataset status.

            if (props.isNewDataset()) {
                throw new BadRequestException(ErrorCode.DATASET_NAME_USED);
            } else {
                DatasetMetadata dataset = datasetOptional.get();
                datasetGuard.check(DatasetAction.UPLOAD, dataset);

                dataset.setStatus(DatasetStatus.UPLOADING);

                // When the dataset exists, the important fields props to update should come
                // from the dataset queried from the mongo db
                props.setRecordDateColumnName(dataset.getRecordDateColumnName());
                props.setRecordSymbolColumnName(dataset.getRecordSymbolName());
                props.setDatasetId(dataset.getId());
                return dataset;
            }
        }

        // Newly created metadata
        VersionControl current = VersionControl.builder()
                .version(0)
                .headers(new ArrayList<>())
                .rowCount(0L)
                .build();

        // Receive indexes
        DatasetMetadata newDataset = DatasetMetadata.builder()
                .userId(props.getUserId())
                .datasetName(props.getDatasetName())
                .status(DatasetStatus.UPLOADING)
                .current(current)
                .staged(null)
                .recordDateColumnName(props.getRecordDateColumnName())
                .recordSymbolName(props.getRecordSymbolColumnName())
                .build();
        logger.info("Find or created dataset for datasetname {}", props.getDatasetName());
        return newDataset;
    }
}
