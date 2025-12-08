package com.example.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Dataset not found");
        }

        @Test
        void testQueryDatapointsWhenCurrentVersionIsNull() {
                Long userId = 1L;
                FetchRecordsProps props = FetchRecordsProps.builder().datasetName("my-ds").build();

                DatasetMetadata meta = new DatasetMetadata();
                meta.setCurrent(null);

                assertThatThrownBy(() -> service.queryDatapoints(userId, props))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("no current version");
        }

        @Test
        void testQueryDatapointsWithoutSymbols() throws Exception {
                Long userId = 1L;
                String datasetName = "prices";
                String datasetId = "dataset-1";
                List<String> list = List.of("close", "volume");

                FetchRecordsProps props = FetchRecordsProps.builder()
                                .datasetName(datasetName)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 1, 31))
                                .columns(list)
                                .symbols(null)
                                .build();

                DatasetMetadata meta = DatasetMetadata.builder()
                                .userId(userId)
                                .id(datasetId)
                                .current(VersionControl.builder().version(2).build())
                                .build();

                DatasetRecord r1 = new DatasetRecord();
                r1.setSymbol("AAPL");
                r1.setRecordDate(LocalDate.of(2024, 1, 5));

                DatasetRecord r2 = new DatasetRecord();
                r2.setSymbol("AAPL");
                r2.setRecordDate(LocalDate.of(2024, 1, 6));

                DatasetRecord r3 = new DatasetRecord();
                r3.setSymbol("MSFT");
                r3.setRecordDate(LocalDate.of(2024, 1, 5));

                List<DatasetRecord> records = List.of(r1, r2, r3);

                when(recordRepo.findByDatasetIdAndVersionAndUploadDateBetween(
                                eq(datasetId),
                                eq(2),
                                eq(props.getStartDate()),
                                eq(props.getEndDate()),
                                any(Sort.class))).thenReturn(records);

                when(datasetRepo.findByUserIdAndDatasetName(userId, datasetName))
                                .thenReturn(meta);

                FetchRecordsResp resp = service.queryDatapoints(userId, props);

                verify(recordRepo, times(1)).findByDatasetIdAndVersionAndUploadDateBetween(
                                eq(datasetId),
                                eq(2),
                                eq(props.getStartDate()),
                                eq(props.getEndDate()),
                                any(Sort.class));

                assertThat(resp.getDatasetName()).isEqualTo(datasetName);
                assertThat(resp.getColumns()).containsExactly("close", "volume");

                Map<String, List<DataPoint>> dp = resp.getDatapoints();
                assertThat(dp.keySet()).containsExactly("AAPL", "MSFT");
                assertThat(dp.get("AAPL")).hasSize(2);
                assertThat(dp.get("MSFT")).hasSize(1);
        }
}
