package com.example.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.example.models.DataProps;
import com.example.repos.ColumnType;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadata.ColumnMeta;
import com.example.repos.DatasetMetadata.VersionControl;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetRecordRepo;
import com.example.repos.DatasetStatus;

@ExtendWith(MockitoExtension.class)
public class UploadServiceTests {
        @Mock
        private DatasetMetadataRepo metadataRepo;

        @InjectMocks
        private UploadService service;

        @Mock
        private DatasetRecordRepo recordRepo;

        @Mock
        private DatasetBuilder builder;

        @Test
        void testPromoteStagedToCurrentShouldThrowExceptionwhenDatasetNotFound() {
                // given
                Long userId = 1L;
                String datasetName = "test-dataset";
                long importedRows = 10L;

                when(metadataRepo.findByUserIdAndDatasetName(userId, datasetName))
                                .thenReturn(null);

                // when & then
                assertThrows(Exception.class,
                                () -> service.promoteStagedToCurrent(datasetName, userId, importedRows));

                verify(metadataRepo, never()).save(any());
        }

        @Test
        void testPromoteStagedToCurrentShouldCopyStagedIntoCurrentAndUpdateMetadata() throws Exception {
                Long userId = 1L;
                String datasetName = "test-dataset";
                long importedRows = 50L;

                ColumnMeta currCol = ColumnMeta.builder()
                                .columnName("old_col")
                                .dataType(ColumnType.STRING)
                                .metric(false)
                                .build();

                // current version
                VersionControl current = VersionControl.builder()
                                .version(0)
                                .headers(List.of(currCol))
                                .rowCount(100L)
                                .build();

                // Stage version
                ColumnMeta stagedCol1 = ColumnMeta.builder()
                                .columnName("col_a")
                                .dataType(ColumnType.NUMBER)
                                .metric(true)
                                .build();

                ColumnMeta stagedCol2 = ColumnMeta.builder()
                                .columnName("col_b")
                                .dataType(ColumnType.STRING)
                                .metric(false)
                                .build();

                VersionControl staged = VersionControl.builder()
                                .version(1)
                                .headers(List.of(stagedCol1, stagedCol2))
                                .rowCount(200L)
                                .build();

                Instant oldUpdatedAt = Instant.parse("2024-01-01T00:00:00Z");

                DatasetMetadata dataset = DatasetMetadata.builder()
                                .userId(userId)
                                .datasetName(datasetName)
                                .status(DatasetStatus.UPLOADING)
                                .createdAt(oldUpdatedAt)
                                .updatedAt(oldUpdatedAt)
                                .current(current)
                                .staged(staged)
                                .build();

                when(metadataRepo.findByUserIdAndDatasetName(userId, datasetName))
                                .thenReturn(Optional.of(dataset));

                // when
                service.promoteStagedToCurrent(datasetName, userId, importedRows);

                // then
                // 1) version / headers / rowCount replaced successfully
                assertEquals(1, dataset.getCurrent().getVersion());
                assertEquals(staged.getHeaders(), dataset.getCurrent().getHeaders());
                assertEquals(staged.getRowCount() + importedRows,
                                dataset.getCurrent().getRowCount());

                // 2) clean staged
                assertNull(dataset.getStaged());

                // 3) change status to ACTIVE
                assertEquals(DatasetStatus.ACTIVE, dataset.getStatus());

                // 4) updatedAt is updated
                assertNotNull(dataset.getUpdatedAt());
                assertNotEquals(oldUpdatedAt, dataset.getUpdatedAt());

                // 5) called save
                verify(metadataRepo, times(1)).save(dataset);
        }

        @Test
        void appendRecordsHappyPath() {
                String datasetId = "1234";

                // prepare CSV
                // header: symbol,price,date
                String csvContent = String.join("\n",
                                "symbol,price,date",
                                "AAPL,100,2024-01-01",
                                "MSFT,200,2024-01-02");
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "stock.csv",
                                "text/csv",
                                csvContent.getBytes(StandardCharsets.UTF_8));

                // Prepare data props
                DataProps props = DataProps.builder().userId(123L).datasetName("daily_stock").build();

                // Prepare dataset to be formed
                VersionControl current = VersionControl.builder()
                                .version(0)
                                .headers(new ArrayList<>())
                                .rowCount(0L)
                                .build();
                DatasetMetadata dataset = DatasetMetadata.builder()
                                .id(datasetId)
                                .current(current)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .status(DatasetStatus.ACTIVE)
                                .build();

                // 1) createIfNotPresentDatasetMetadata return the mocked dataset
                when(builder.createIfNotPresentDatasetMetadata(any(DataProps.class), eq(metadataRepo)))
                                .thenReturn(dataset);

                // 2) mergeAndFillInferNeededColumns：columnsNeedsInfer not empty，go to
                // inferAndfillStagedColumns branch
                doAnswer(invocation -> {
                        @SuppressWarnings("unchecked")
                        Set<String> columnsNeedsInfer = (Set<String>) invocation.getArgument(0);
                        columnsNeedsInfer.add("price");
                        columnsNeedsInfer.add("date");

                        return null;
                }).when(builder).mergeAndFillInferNeededColumns(anySet(), eq(dataset), anyList());
                // 3) recordRepo.bulkInsertRecords
                doNothing().when(recordRepo).bulkInsertRecords(anyList(), any(DataProps.class));

                // 5) append records
                long inserted = service.appendRecords(file, props);

                // Assert
                assertEquals(inserted, 2L);

                verify(builder).createIfNotPresentDatasetMetadata(props, metadataRepo);
                verify(builder).mergeAndFillInferNeededColumns(anySet(), eq(dataset), argThat(headers -> {
                        assertThat(headers).containsExactly("symbol", "price", "date");
                        return true;
                }));

                verify(builder).inferAndfillStagedColumns(eq(dataset), anyList(), anySet());
                verify(metadataRepo).save(eq(dataset));

                assertThat(props.getDatasetId()).isEqualTo(datasetId);
                assertThat(props.getStagedVersion()).isEqualTo(1L); // current.version(0) + 1

                @SuppressWarnings("unchecked")
                ArgumentCaptor<List<Map<String, String>>> rowsCaptor = ArgumentCaptor.forClass(List.class);

                verify(recordRepo, times(1)).bulkInsertRecords(rowsCaptor.capture(), eq(props));

                List<Map<String, String>> insertedRows = rowsCaptor.getValue();
                assertThat(insertedRows).hasSize(2);

                // Check upper case
                Map<String, String> row1 = insertedRows.get(0);
                assertThat(row1.keySet())
                                .containsExactlyInAnyOrder("SYMBOL", "PRICE", "DATE");
                assertThat(row1.get("SYMBOL")).isEqualTo("AAPL");
                assertThat(row1.get("PRICE")).isEqualTo("100");
                assertThat(row1.get("DATE")).isEqualTo("2024-01-01");

                Map<String, String> row2 = insertedRows.get(1);
                assertThat(row2.get("SYMBOL")).isEqualTo("MSFT");
                assertThat(row2.get("PRICE")).isEqualTo("200");
                assertThat(row2.get("DATE")).isEqualTo("2024-01-02");
        }
}
