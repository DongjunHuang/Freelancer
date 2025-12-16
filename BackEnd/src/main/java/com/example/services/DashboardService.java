package com.example.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.models.FetchRecordsProps;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetRecord;
import com.example.repos.DatasetRecordRepo;
import com.example.repos.MongoKeys;
import com.example.requests.DataPoint;
import com.example.requests.FetchRecordsResp;

import lombok.RequiredArgsConstructor;

/**
 * The dashboard service to handle the functions from the user to request the
 * information of datapoints
 * and datasets.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final DatasetMetadataRepo datasetRepo;
    private final DatasetRecordRepo recordRepo;

    /**
     * Get user datastss.
     * 
     * @param userId the user id.
     * @return the dataset metadta.
     */
    public List<DatasetMetadata> getUserDatasets(Long userId) {
        return datasetRepo.findByUserId(userId);
    }

    /**
     * According to user requirement, find corresponding datapoints to users.
     * TODO: we can not return full list at once, returning paging is necessary.
     * 
     * @param userId
     * @param props
     * @return
     */
    public FetchRecordsResp queryDatapoints(Long userId, FetchRecordsProps props) {
        DatasetMetadata meta = datasetRepo.findByUserIdAndDatasetName(userId, props.getDatasetName())
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATASET_NOT_FOUND));

        String datasetId = meta.getId();
        Integer version = meta.getCurrent().getVersion();

        // parse the symbols to fetch
        List<DatasetRecord> records = null;
        Sort sort = Sort.by(
                Sort.Order.asc(MongoKeys.Record.RECORD_DATE),
                Sort.Order.asc(MongoKeys.Record.SYMBOL));
        logger.info("The symbols found {} with datasetid {}, version {}, start date {}, enddate {}",
                props.getSymbols(),
                datasetId,
                version,
                props.getStartDate(),
                props.getEndDate());
        if (props.getSymbols() == null || props.getSymbols().isEmpty()) {
            records = recordRepo.findByDatasetIdAndVersionAndUploadDateBetween(
                    datasetId,
                    version,
                    props.getStartDate(),
                    props.getEndDate(),
                    sort);
        } else {
            records = recordRepo.findByDatasetIdAndVersionAndUploadDateBetweenAndSymbols(
                    datasetId,
                    version,
                    props.getStartDate(),
                    props.getEndDate(),
                    props.getSymbols(),
                    sort);
        }
        if (records == null) {
            // The defensive check for null
            throw new IllegalStateException("Repository returned null list");
        }

        // Make up data sets
        List<DataPoint> datapoints = records.stream()
                .flatMap(r -> DatasetRecord.toDataPoints(r, props.getColumns()).stream())
                .toList();
        logger.info("Total number datapoints {} found from the number of records {} for request {}", datapoints.size(),
                records.size(), props);

        FetchRecordsResp resp = new FetchRecordsResp();
        resp.setDatasetName(props.getDatasetName());
        resp.setColumns(props.getColumns());
        resp.setDatapoints(datapoints);
        return resp;
    }
}
