package com.example.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MetadataServiceTests {

    @InjectMocks
    private MetadataService metadataService;

    @Mock
    private DatasetMetadataRepo metadataRepo;

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
}