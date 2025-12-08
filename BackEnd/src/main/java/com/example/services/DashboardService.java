package com.example.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public FetchRecordsResp queryDatapoints(Long userId, FetchRecordsProps props) throws Exception {
        DatasetMetadata meta = datasetRepo.findByUserIdAndDatasetName(userId, props.getDatasetName());
        if (meta == null || meta.getCurrent() == null) {
            throw new IllegalArgumentException("Dataset not found or no current version");
        }

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
            throw new Exception("Can not find any data points");
        }
        logger.info("Total number {} found from the database for request {}", records.size(), props);

        // Make up data sets
        Map<String, List<DataPoint>> datapoints = records.stream().collect(Collectors.groupingBy(
                DatasetRecord::getSymbol,
                LinkedHashMap::new,
                Collectors.mapping(
                        (r) -> DatasetRecord.toDataPoint(r, props.getColumns()),
                        Collectors.toList())));

        FetchRecordsResp resp = new FetchRecordsResp();
        resp.setDatasetName(props.getDatasetName());
        resp.setColumns(props.getColumns());
        resp.setDatapoints(datapoints);
        return resp;
    }
}
