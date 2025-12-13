package com.example.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.exception.NotFoundException;
import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetRecordRepo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetadataServiceTests {

    @InjectMocks
    private MetadataService metadataService;

    @Mock
    private DatasetMetadataRepo metadataRepo;

    @Mock
    private DatasetRecordRepo recordRepo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserDatasetsShouldReturnUserDatasets() {
        Long userId = 1L;

        DatasetMetadata d1 = DatasetMetadata.builder().userId(userId).datasetName("AAPL").build();
        DatasetMetadata d2 = DatasetMetadata.builder().userId(userId).datasetName("GOOGL").build();

        when(metadataRepo.findByUserId(userId))
                .thenReturn(Arrays.asList(d1, d2));

        List<DatasetMetadata> result = metadataService.getUserDatasets(userId);

        assertThat(result)
                .hasSize(2)
                .extracting(DatasetMetadata::getDatasetName)
                .containsExactly("AAPL", "GOOGL");

        verify(metadataRepo).findByUserId(userId);
    }

    @Test
    void testGetUserDatasetsWhenEmpty() {
        Long userId = 2L;

        when(metadataRepo.findByUserId(userId))
                .thenReturn(Collections.emptyList());

        List<DatasetMetadata> result = metadataService.getUserDatasets(userId);

        assertThat(result).isEmpty();

        verify(metadataRepo).findByUserId(userId);
    }

    @Test
    void testDeleteDatasetByNameAndUserIdWhenValid() {
        // given
        Long userId = 123L;
        String datasetName = "my-dataset";
        String setId = "setId";

        DatasetMetadata meta = new DatasetMetadata();
        meta.setId(setId);

        when(metadataRepo.findByUserIdAndDatasetName(userId, datasetName))
                .thenReturn(Optional.of(meta));

        // when
        metadataService.deleteDatasetByNameAndUserId(datasetName, userId);

        InOrder inOrder = inOrder(recordRepo, metadataRepo);
        inOrder.verify(recordRepo).deleteByDatasetId(meta.getId());
        inOrder.verify(metadataRepo).delete(meta);
    }

    @Test
    void testDeleteDatasetByNameAndUserIdWhenThrowNotFoundException() {
        // given
        Long userId = 123L;
        String datasetName = "non-exist";

        when(metadataRepo.findByUserIdAndDatasetName(userId, datasetName))
                .thenReturn(Optional.empty());

        // when & then
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> metadataService.deleteDatasetByNameAndUserId(datasetName, userId));

        verify(recordRepo, never()).deleteByDatasetId(any());
        verify(metadataRepo, never()).delete(any());
    }
}