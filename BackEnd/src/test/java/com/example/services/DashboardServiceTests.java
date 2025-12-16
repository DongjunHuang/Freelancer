package com.example.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.models.FetchRecordsProps;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadata.VersionControl;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetRecord;
import com.example.repos.DatasetRecordRepo;
import com.example.requests.DataPoint;
import com.example.requests.FetchRecordsResp;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTests {

        @Mock
        DatasetMetadataRepo datasetRepo;

        @Mock
        DatasetRecordRepo recordRepo;

        @InjectMocks
        DashboardService service;

        @Test
        void testGetUserDatasetsValid() {
                Long userId = 123L;
                DatasetMetadata m1 = new DatasetMetadata();
                DatasetMetadata m2 = new DatasetMetadata();
                List<DatasetMetadata> list = List.of(m1, m2);

                when(datasetRepo.findByUserId(userId)).thenReturn(list);

                List<DatasetMetadata> result = service.getUserDatasets(userId);

                verify(datasetRepo, times(1)).findByUserId(userId);
                assertThat(result).containsExactly(m1, m2);
        }

        @Test
        void testQueryDatapointsWhenDatasetNotFound() {
                Long userId = 1L;
                FetchRecordsProps props = FetchRecordsProps.builder().datasetName("my-ds").build();

                assertThatThrownBy(() -> service.queryDatapoints(userId, props))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining(ErrorCode.DATASET_NOT_FOUND.getMessage());
        }

        @Test
        void testQueryDatapointsWhenCurrentVersionIsNull() {
                Long userId = 1L;
                FetchRecordsProps props = FetchRecordsProps.builder().datasetName("my-ds").build();

                DatasetMetadata meta = new DatasetMetadata();
                meta.setCurrent(null);

                assertThatThrownBy(() -> service.queryDatapoints(userId, props))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining(ErrorCode.DATASET_NOT_FOUND.getMessage());
        }

        @Test
        void testQueryDatapointsWithoutSymbols() throws Exception {
                Long userId = 1L;
                String datasetName = "prices";
                String datasetId = "dataset-1";
                List<String> columns = List.of("close", "volume");

                // Prepare request props
                FetchRecordsProps props = FetchRecordsProps.builder()
                                .datasetName(datasetName)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 1, 31))
                                .columns(columns)
                                .symbols(null)
                                .build();

                // Prepare dataset meta
                DatasetMetadata meta = DatasetMetadata.builder()
                                .userId(userId)
                                .id(datasetId)
                                .current(VersionControl.builder().version(2).build())
                                .build();

                // Prepare data record
                Map<String, String> data1 = new HashMap<>();
                data1.put("close", "1234");
                data1.put("volume", "3455");
                Map<String, String> data2 = new HashMap<>();
                data2.put("close", "1234");
                data2.put("volume", "3455");
                data2.put("pick", "3455");
                Map<String, String> data3 = new HashMap<>();
                data3.put("close", "1234");
                data3.put("volume", "3455");
                data3.put("down", "pppp");

                DatasetRecord r1 = DatasetRecord.builder().symbol("AAPL").data(data1)
                                .recordDate(LocalDate.of(2024, 1, 5)).build();
                DatasetRecord r2 = DatasetRecord.builder().symbol("AAPL").data(data2)
                                .recordDate(LocalDate.of(2024, 1, 6)).build();
                DatasetRecord r3 = DatasetRecord.builder().symbol("MSFT").data(data3)
                                .recordDate(LocalDate.of(2024, 1, 5)).build();
                List<DatasetRecord> records = List.of(r1, r2, r3);

                when(recordRepo.findByDatasetIdAndVersionAndUploadDateBetween(
                                eq(datasetId),
                                eq(2),
                                eq(props.getStartDate()),
                                eq(props.getEndDate()),
                                any(Sort.class))).thenReturn(records);

                when(datasetRepo.findByUserIdAndDatasetName(userId, datasetName))
                                .thenReturn(Optional.of(meta));

                FetchRecordsResp resp = service.queryDatapoints(userId, props);

                verify(recordRepo, times(1)).findByDatasetIdAndVersionAndUploadDateBetween(
                                eq(datasetId),
                                eq(2),
                                eq(props.getStartDate()),
                                eq(props.getEndDate()),
                                any(Sort.class));

                assertThat(resp.getDatasetName()).isEqualTo(datasetName);
                assertThat(resp.getColumns()).containsExactly("close", "volume");

                List<DataPoint> datapoints = resp.getDatapoints();
                assertThat(datapoints.size()).isEqualTo(6);

        }
}
