package com.example.background;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.repos.DatasetMetadataRepo;
import com.example.repos.DatasetStatus;

class DatasetStatusMonitorTests {

    private DatasetMetadataRepo repo;
    private DatasetStatusMonitor monitor;

    @BeforeEach
    void setUp() {
        repo = mock(DatasetMetadataRepo.class);

        Instant fixedNow = Instant.parse("2025-12-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        monitor = new DatasetStatusMonitor(repo, fixedClock);
    }

    @Test
    void monitorStuckDatasets_shouldQueryUploadingAndDeletingWithCorrectCutoffs() {
        when(repo.findByStatusInAndUpdatedAtBefore(anyList(), any(Instant.class)))
                .thenReturn(List.of());

        monitor.monitorStuckDatasets();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DatasetStatus>> statusesCap = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Instant> cutoffCap = ArgumentCaptor.forClass(Instant.class);

        verify(repo, times(2)).findByStatusInAndUpdatedAtBefore(statusesCap.capture(), cutoffCap.capture());

        var statuses = statusesCap.getAllValues();
        var cutoffs = cutoffCap.getAllValues();

        assertThat(statuses).containsExactly(
                List.of(DatasetStatus.UPLOADING),
                List.of(DatasetStatus.DELETING));

        Instant now = Instant.parse("2025-12-17T12:00:00Z");
        assertThat(cutoffs).containsExactly(
                now.minus(Duration.ofMinutes(30)),
                now.minus(Duration.ofMinutes(10)));
    }
}