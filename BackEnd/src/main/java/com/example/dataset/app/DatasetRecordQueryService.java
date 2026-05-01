package com.example.dataset.app;

import com.example.dataset.domain.dto.Datapoint;
import com.example.dataset.domain.DatasetMetadata;
import com.example.dataset.domain.DatasetRecord;
import com.example.dataset.domain.dto.QueryRecordsResp;
import com.example.dataset.domain.dto.QueryRecordsReq;
import com.example.dataset.infra.mongo.DatasetRecordRepo;
import com.example.dataset.infra.mongo.MongoKeys;
import com.example.guards.DatasetAction;
import com.example.guards.DatasetStateGuard;
import com.example.utils.DateParser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasetRecordQueryService {
    private static final Logger logger = LoggerFactory.getLogger(DatasetRecordQueryService.class);
    private final DatasetRecordRepo recordRepo;
    private final DatasetStateGuard stateGuard;
    private final DateParser dateParser;

    public QueryRecordsResp queryRecords(Long userId, String datasetId, QueryRecordsReq req) {
        // We only need to check the status not changing the status.
        DatasetMetadata dataset = stateGuard.loadAndCheck(userId, datasetId, DatasetAction.QUERY);

        String dsId = dataset.getId();
        Integer version = dataset.getCurrent().getVersion();
        String timezone = dataset.getTimezone();

        List<String> columnsUpperCase = new ArrayList<>();
        if (req.getColumns() != null) {
            for (int i = 0; i < req.getColumns().size(); i++) {
                columnsUpperCase.add(req.getColumns().get(i).toUpperCase());
            }
        }

        List<String> symbols = null;
        if (req.getSymbols() != null) {
            symbols = Arrays.stream(req.getSymbols().split("[,\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
        }

        // parse the symbols to fetch
        List<DatasetRecord> records = null;
        Sort sort = Sort.by(
                Sort.Order.asc(MongoKeys.Record.USER_DEFINED_TIME),
                Sort.Order.asc(MongoKeys.Record.INDEXED_VALUE));

        Instant startTime = parseLocalDate(req.getStartDate(), timezone);
        Instant endTime = parseLocalDate(req.getEndDate(), timezone);

        if (symbols == null || symbols.isEmpty()) {
            records = recordRepo.findByDatasetIdAndVersionLteAndUserDefinedTimeRange(
                    datasetId,
                    version,
                    startTime,
                    endTime,
                    sort);
        } else {
            records = recordRepo.findByDatasetIdAndVersionLteAndUserDefinedTimeRangeAndIndexedValues(
                    datasetId,
                    version,
                    startTime,
                    endTime,
                    symbols,
                    sort);
        }

        if (records == null) {
            // The defensive check for null
            throw new IllegalStateException("Repository returned null list");
        }

        List<Datapoint> dataPoints = records
                .stream()
                .flatMap(r -> toRecord(r, req.getColumns(), timezone).stream())
                .toList();

        // ===========================================================================================
        QueryRecordsResp resp = new QueryRecordsResp();
        resp.setDatasetName(dsId);
        resp.setColumns(req.getColumns());
        resp.setRecords(dataPoints);
        return resp;
    }

    public Instant parseLocalDate(LocalDate raw, String timezone) {
        if (raw == null) {
            return null;
        }

        ZoneId zone = resolveZone(timezone);
        return raw.atStartOfDay(zone).toInstant();
    }

    private ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone, e);
        }
    }

    public LocalDate toLocalDate(Instant instant, String timezone) {
        if (instant == null) {
            return null;
        }
        ZoneId zone = resolveZone(timezone);
        return instant.atZone(zone).toLocalDate();
    }

    public List<Datapoint> toRecord(DatasetRecord record, List<String> columns, String timezone) {
        List<Datapoint> out = new ArrayList<>();

        if (record.getData() == null) {
            return out;
        }

        for (String col : columns) {
            String value = record.getData().get(col);
            if (value != null) {
                out.add(Datapoint.builder()
                        .recordedTime(toLocalDate(record.getUserDefinedTime(), timezone))
                        .symbol(record.getIndexedValue())
                        .column(col)
                        .value(Double.parseDouble(value))
                        .build());
            }
        }

        return out;
    }
}
