package com.guards;

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.common.dataset.domain.DatasetMetadata;
import com.example.common.dataset.infra.mongo.DatasetMetadataRepo;
import com.example.exception.types.DatasetStatusException;
import com.example.exception.ErrorCode;
import com.example.guards.DatasetAction;
import com.example.guards.DatasetStateGuard;
import com.example.upload.domain.DatasetStatus;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DatasetStateGuardTests {
    @Mock
    DatasetMetadataRepo repo;

    DatasetStateGuard guard;

    @BeforeEach
    void setup() {
        guard = new DatasetStateGuard(repo);
    }

    @Test
    void shouldBeAllowedWhenActive() {
        Long userId = 1L;
        String datasetName = "test-dataset";
        DatasetMetadata ds = DatasetMetadata.builder().userId(userId).datasetName(datasetName)
                .status(DatasetStatus.ACTIVE)
                .build();
        when(repo.findByUserIdAndDatasetName(userId, datasetName)).thenReturn(Optional.of(ds));
        DatasetMetadata ds2 = guard.loadAndCheck(userId, datasetName, DatasetAction.QUERY);
        assertThat(ds2.getUserId()).isEqualTo(userId);
        assertThat(ds2.getDatasetName()).isEqualTo(datasetName);

    }

    @Test
    void shouldRejectWhenDeleting() {
        Long userId = 1L;
        String datasetName = "test-dataset";
        DatasetMetadata ds = DatasetMetadata.builder().userId(userId).datasetName(datasetName)
                .status(DatasetStatus.DELETING)
                .build();
        when(repo.findByUserIdAndDatasetName(userId, datasetName)).thenReturn(Optional.of(ds));
        assertThatThrownBy(() -> guard.loadAndCheck(userId, datasetName, DatasetAction.QUERY))
                .isInstanceOf(DatasetStatusException.class)
                .hasMessageContaining(ErrorCode.DATASET_NOT_AVAILABLE.getMessage());
    }
}