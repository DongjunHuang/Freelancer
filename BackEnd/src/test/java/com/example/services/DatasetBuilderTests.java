package com.example.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.models.DataProps;
import com.example.repos.ColumnType;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadata.ColumnMeta;
import com.example.repos.DatasetMetadata.VersionControl;
import com.example.repos.DatasetMetadataRepo;

public class DatasetBuilderTests {
        DatasetBuilder builder = new DatasetBuilder();

        @Test
        void testNonNumberTypeAlwaysFalse() {
                assertFalse(builder.isMetricColumn("price", ColumnType.STRING));
                assertFalse(builder.isMetricColumn("amount", ColumnType.BOOLEAN));
                assertFalse(builder.isMetricColumn(null, ColumnType.STRING));
        }

        @Test
        void testNumberTypeWithNullHeaderIsMetric() {
                boolean result = builder.isMetricColumn(null, ColumnType.NUMBER);
                assertFalse(result);
        }

        @Test
        void testNumberTypeWithNormalHeaderIsMetric() {
                assertTrue(builder.isMetricColumn("price", ColumnType.NUMBER));
                assertTrue(builder.isMetricColumn("  closing_value  ", ColumnType.NUMBER));
                assertTrue(builder.isMetricColumn("TOTAL_REVENUE", ColumnType.NUMBER));
        }

        @Test
        void testNumberTypeWithNonMetricHeaderReturnsFalse() {
                assertFalse(builder.isMetricColumn("user_id", ColumnType.NUMBER));
                assertFalse(builder.isMetricColumn("  customerId  ", ColumnType.NUMBER));
                assertFalse(builder.isMetricColumn("order_id_number", ColumnType.NUMBER));
        }

        @Test
        void testHeaderCheckIsCaseInsensitiveAndTrimmed() {
                assertFalse(builder.isMetricColumn("  TradeDATE  ", ColumnType.NUMBER));
                assertFalse(builder.isMetricColumn("CREATED_DATE", ColumnType.NUMBER));
        }

        @Test
        void testHeaderContainingNonMetricSubstringInMiddleAlsoFalse() {
                assertFalse(builder.isMetricColumn("product_code_value", ColumnType.NUMBER));
                assertFalse(builder.isMetricColumn("CODE_metric", ColumnType.NUMBER));
        }

        @Test
        void testExistingDatasetReturnedWhenNotNew() throws Exception {
                DatasetMetadataRepo repo = Mockito.mock(DatasetMetadataRepo.class);

                DataProps props = DataProps.builder()
                                .newDataset(false)
                                .userId(1L)
                                .datasetName("prices")
                                .build();

                DatasetMetadata existing = DatasetMetadata.builder()
                                .id("UserId")
                                .userId(1L)
                                .datasetName("prices")
                                .recordDateColumnName("date")
                                .recordSymbolName("symbol")
                                .current(VersionControl.builder()
                                                .version(1)
                                                .headers(new ArrayList<>())
                                                .rowCount(100L)
                                                .build())
                                .build();

                when(repo.findByUserIdAndDatasetName(1L, "prices")).thenReturn(existing);

                // --- Act ---
                DatasetMetadata result = builder.createIfNotPresentDatasetMetadata(props, repo);

                // --- Assert ---
                assertNotNull(result);
                assertEquals("UserId", result.getId());
                assertEquals("date", props.getRecordDateColumnName());
                assertEquals("symbol", props.getRecordSymbolColumnName());

                verify(repo).findByUserIdAndDatasetName(1L, "prices");
        }

        @Test
        void testNewDatasetCreatedWhenPropsIsNew() throws Exception {
                // --- Arrange ---
                DatasetMetadataRepo repo = Mockito.mock(DatasetMetadataRepo.class);

                DataProps props = DataProps.builder()
                                .newDataset(true)
                                .userId(1L)
                                .datasetName("new_prices")
                                .recordDateColumnName("date")
                                .recordSymbolColumnName("code")
                                .build();

                // --- Act ---
                DatasetMetadata result = builder.createIfNotPresentDatasetMetadata(props, repo);

                // --- Assert ---
                assertNotNull(result);
                assertEquals("new_prices", result.getDatasetName());
                assertEquals("date", result.getRecordDateColumnName());
                assertEquals("code", result.getRecordSymbolName());

                assertNotNull(result.getCurrent());
                assertEquals(0, result.getCurrent().getVersion());
                assertEquals(0, result.getCurrent().getRowCount());
                assertTrue(result.getCurrent().getHeaders().isEmpty());

                verify(repo, never()).findByUserIdAndDatasetName(anyLong(), anyString());
        }

        @Test
        void testMergeAndFillInferNeededColumnShouldCopyExistingAndAddNewColumns() throws Exception {
                // Given col_a(NUMBER, metric)，col_b(STRING, non-metric)
                ColumnMeta colA = ColumnMeta.builder()
                                .columnName("col_a")
                                .dataType(ColumnType.NUMBER)
                                .metric(true)
                                .build();

                ColumnMeta colB = ColumnMeta.builder()
                                .columnName("col_b")
                                .dataType(ColumnType.STRING)
                                .metric(false)
                                .build();

                VersionControl current = VersionControl.builder()
                                .version(3)
                                .rowCount(100L)
                                .headers(new ArrayList<>(List.of(colA, colB)))
                                .build();

                DatasetMetadata dataset = DatasetMetadata.builder()
                                .datasetName("test_dataset")
                                .current(current)
                                .staged(null)
                                .build();

                List<String> newHeaders = List.of("col_a", "col_c");

                Set<String> columnsNeedsInfer = new HashSet<>();
                columnsNeedsInfer.add("already_here");

                // when
                builder.mergeAndFillInferNeededColumns(columnsNeedsInfer, dataset, newHeaders);

                // then
                assertNotNull(dataset.getStaged());
                VersionControl staged = dataset.getStaged();

                assertEquals(4, staged.getVersion());
                assertEquals(100, staged.getRowCount());

                // 2) staged.headers should contain：col_a, col_b（have） + col_c（new）
                assertEquals(3, staged.getHeaders().size());

                Map<String, ColumnMeta> byName = new HashMap<>();
                for (ColumnMeta c : staged.getHeaders()) {
                        byName.put(c.getColumnName(), c);
                }

                assertTrue(byName.containsKey("col_a"));
                assertTrue(byName.containsKey("col_b"));
                assertTrue(byName.containsKey("col_c"));

                // The original should not change
                ColumnMeta stagedA = byName.get("col_a");
                assertEquals(ColumnType.NUMBER, stagedA.getDataType());
                assertTrue(stagedA.isMetric());

                ColumnMeta stagedC = byName.get("col_c");
                assertEquals(ColumnType.STRING, stagedC.getDataType());
                assertFalse(stagedC.isMetric());

                assertEquals(2, columnsNeedsInfer.size());
                assertTrue(columnsNeedsInfer.contains("already_here"));
                assertTrue(columnsNeedsInfer.contains("col_c"));
        }

        @Test
        void testMergeAndFillInferNeededColumnNoNewColumns() {
                // given
                ColumnMeta colA = ColumnMeta.builder()
                                .columnName("col_a")
                                .dataType(ColumnType.NUMBER)
                                .metric(true)
                                .build();

                VersionControl current = VersionControl.builder()
                                .version(1)
                                .rowCount(10L)
                                .headers(new ArrayList<>(List.of(colA)))
                                .build();

                DatasetMetadata dataset = DatasetMetadata.builder()
                                .datasetName("test_dataset")
                                .current(current)
                                .staged(null)
                                .build();

                // headers should be the same
                List<String> headers = List.of("col_a");

                Set<String> columnsNeedsInfer = new HashSet<>(Set.of("exists"));

                // when
                builder.mergeAndFillInferNeededColumns(columnsNeedsInfer, dataset, headers);

                // then
                VersionControl staged = dataset.getStaged();
                assertNotNull(staged);
                assertEquals(2, staged.getVersion());
                assertEquals(10L, staged.getRowCount());

                // headers should only have "col_a"
                assertEquals(1, staged.getHeaders().size());
                assertEquals("col_a", staged.getHeaders().get(0).getColumnName());

                // No new columns should be added to infer set
                assertEquals(1, columnsNeedsInfer.size());
                assertTrue(columnsNeedsInfer.contains("exists"));
        }

        @Test
        void mergeAndFillInferNeededColumnsShouldIgnoreDuplicatedNewHeaders() {
                // given
                VersionControl current = VersionControl.builder()
                                .version(0)
                                .rowCount(0L)
                                .headers(new ArrayList<>())
                                .build();

                DatasetMetadata dataset = DatasetMetadata.builder()
                                .datasetName("test_dataset")
                                .current(current)
                                .staged(null)
                                .build();

                List<String> headers = List.of("value", "value", "value");

                Set<String> columnsNeedsInfer = new HashSet<>();

                // when
                builder.mergeAndFillInferNeededColumns(columnsNeedsInfer, dataset, headers);

                // then
                VersionControl staged = dataset.getStaged();
                assertNotNull(staged);

                assertEquals(1, staged.getHeaders().size());
                ColumnMeta only = staged.getHeaders().get(0);
                assertEquals("value", only.getColumnName());
                assertEquals(ColumnType.STRING, only.getDataType());
                assertFalse(only.isMetric());

                assertEquals(1, columnsNeedsInfer.size());
                assertTrue(columnsNeedsInfer.contains("value"));
        }

        @Test
        void inferAndfillStagedColumnsShouldInferTypesAndMetricFlags() {
                // 1. Prepare dataset
                VersionControl staged = VersionControl.builder()
                                .version(1)
                                .rowCount(0L)
                                .headers(new ArrayList<>(List.of(
                                                DatasetMetadata.ColumnMeta.builder()
                                                                .columnName("price")
                                                                .dataType(ColumnType.STRING)
                                                                .metric(false)
                                                                .build(),
                                                DatasetMetadata.ColumnMeta.builder()
                                                                .columnName("trade_date")
                                                                .dataType(ColumnType.STRING)
                                                                .metric(false)
                                                                .build(),
                                                DatasetMetadata.ColumnMeta.builder()
                                                                .columnName("user_id")
                                                                .dataType(ColumnType.STRING)
                                                                .metric(false)
                                                                .build(),
                                                DatasetMetadata.ColumnMeta.builder()
                                                                .columnName("note")
                                                                .dataType(ColumnType.STRING)
                                                                .metric(false)
                                                                .build())))
                                .build();

                DatasetMetadata dataset = DatasetMetadata.builder()
                                .datasetName("test-dataset")
                                .current(null)
                                .staged(staged)
                                .build();

                // 2. Prepare several lines of CSV to infer
                List<Map<String, String>> inferRows = List.of(
                                Map.of(
                                                "price", "100.5",
                                                "trade_date", "2024-01-01",
                                                "user_id", "1001",
                                                "note", "first"),
                                Map.of(
                                                "price", "200.75",
                                                "trade_date", "2024-01-02",
                                                "user_id", "1002",
                                                "note", "second"),
                                Map.of(
                                                "price", "1,234.00",
                                                "trade_date", "2024/01/03",
                                                "user_id", "1003",
                                                "note", "third"));

                // 3. The collection of the columns to be infered
                Set<String> columnsNeedsInfer = new HashSet<>(Arrays.asList(
                                "price",
                                "trade_date",
                                "user_id"));

                // 4. Test
                builder.inferAndfillStagedColumns(dataset, inferRows, columnsNeedsInfer);

                // 5. Assert
                ColumnMeta priceCol = findHeader(dataset, "price");
                assertNotNull(priceCol);
                assertEquals(ColumnType.NUMBER, priceCol.getDataType(), "price should be NUMBER");
                assertTrue(priceCol.isMetric(), "price should METRIC column");

                ColumnMeta tradeDateCol = findHeader(dataset, "trade_date");
                assertNotNull(tradeDateCol);
                assertEquals(ColumnType.DATE, tradeDateCol.getDataType(), "trade_date should be DATE");
                assertFalse(tradeDateCol.isMetric(), "should not be METRIC");

                ColumnMeta userIdCol = findHeader(dataset, "user_id");
                assertNotNull(userIdCol);
                assertEquals(ColumnType.STRING, userIdCol.getDataType(), "user_id should be STRING");
                assertFalse(userIdCol.isMetric(), "id should not be METRIC");

                // The not column is not in the infer list should not be infered.
                ColumnMeta noteCol = findHeader(dataset, "note");
                assertNotNull(noteCol);
                assertEquals(ColumnType.STRING, noteCol.getDataType(), "note should be STRING, should not be infered");
                assertFalse(noteCol.isMetric(), "note should not be METRIC");
        }

        private DatasetMetadata.ColumnMeta findHeader(DatasetMetadata dataset, String name) {
                return dataset.getStaged().getHeaders().stream()
                                .filter(c -> name.equals(c.getColumnName()))
                                .findFirst()
                                .orElse(null);
        }
}
