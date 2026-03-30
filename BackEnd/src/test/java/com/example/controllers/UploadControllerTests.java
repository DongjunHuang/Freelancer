package com.example.controllers;

import com.example.common.dataset.domain.DataProps;
import com.example.common.dataset.domain.DatasetMetadata;
import com.example.dashboard.domain.DatasetMetadataResp;
import com.example.exception.types.BadRequestException;
import com.example.exception.ErrorCode;
import com.example.security.JwtUserDetails;
import com.example.upload.app.MetadataService;
import com.example.upload.app.UploadService;
import com.example.upload.domain.DatasetReq;
import com.example.upload.interfaces.UploadController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UploadControllerTests {

    @InjectMocks
    private UploadController controller;

    @Mock
    private UploadService uploadService;

    @Mock
    private Authentication authentication;

    @Mock
    private MetadataService metadataService;

    @Mock
    private JwtUserDetails userDetails;

    @Mock
    private MultipartFile file;

    private DatasetReq datasetReq;

    @BeforeEach
    void setUp() {
        datasetReq = new DatasetReq();
        datasetReq.setDatasetName("my_table");
        datasetReq.setRecordDateColumnName("date");
        datasetReq.setRecordDateColumnFormat("yyyy-MM-dd");
        datasetReq.setRecordSymbolColumnName("symbol");
        datasetReq.setNewDataset(true);
    }

    @Test
    void testUploadWithNewDatasetWhenFileIsNull() {
        assertThatThrownBy(() -> controller.uploadCsv(
                null,
                datasetReq,
                userDetails))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorCode.NOT_VALID_FILE.getMessage());
    }

    @Test
    void testUploadWithNewDatasetWhenFileIsEmpty() {
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> controller.uploadCsv(
                file,
                datasetReq,
                userDetails))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorCode.NOT_VALID_FILE.getMessage());
    }

    @Test
    void testUploadWithNewDatasetValid() throws Exception {
        long userId = 123L;
        String datasetName = "MY_TABLE";
        JwtUserDetails user = mock(JwtUserDetails.class);
        when(user.getId()).thenReturn(userId);
        when(file.isEmpty()).thenReturn(false);
        when(uploadService.appendRecords(any(MultipartFile.class), any(DataProps.class))).thenReturn(42L);

        ResponseEntity<?> resp = controller.uploadCsv(
                file,
                datasetReq,
                user);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body.get("Result")).isEqualTo("Success");

        ArgumentCaptor<DataProps> propsCaptor = ArgumentCaptor.forClass(DataProps.class);
        verify(uploadService).appendRecords(eq(file), propsCaptor.capture());
        DataProps props = propsCaptor.getValue();

        assertThat(props.getUserId()).isEqualTo(userId);
        assertThat(props.getDatasetName()).isEqualTo(datasetName);
        assertThat(props.getRecordDateColumnName()).isEqualTo("DATE");
        assertThat(props.getRecordSymbolColumnName()).isEqualTo("SYMBOL");
        assertThat(props.isNewDataset()).isTrue();
        assertThat(props.getBatchId()).isNotNull();
        assertThat(isValidUUID(props.getBatchId())).isTrue();

        // promoteStagedToCurrent
        verify(uploadService).promoteStagedToCurrent(datasetName, 123L, 42L);
    }

    private boolean isValidUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void testFetchDatasetsValid() {
        long userId = 123L;

        JwtUserDetails user = mock(JwtUserDetails.class);

        // given
        when(user.getId()).thenReturn(userId);

        DatasetMetadata meta1 = DatasetMetadata.builder()
                .id("ds-1")
                .userId(userId)
                .datasetName("table_1")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        DatasetMetadata meta2 = DatasetMetadata.builder()
                .id("ds-2")
                .userId(userId)
                .datasetName("table_2")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<DatasetMetadata> list = List.of(meta1, meta2);
        when(metadataService.getUserDatasets(userId)).thenReturn(list);

        // when
        ResponseEntity<List<DatasetMetadataResp>> resp = controller.fetchDatasets(user);

        // then
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody()).hasSize(2);

        DatasetMetadataResp expected1 = DatasetMetadataResp.fromDatasetMetadata(meta1);
        DatasetMetadataResp expected2 = DatasetMetadataResp.fromDatasetMetadata(meta2);

        assertThat(resp.getBody().get(0))
                .usingRecursiveComparison()
                .isEqualTo(expected1);

        assertThat(resp.getBody().get(1))
                .usingRecursiveComparison()
                .isEqualTo(expected2);

        verify(metadataService).getUserDatasets(userId);
        verifyNoMoreInteractions(metadataService);
    }

    @Test
    void testFetchDatasetsWhenNoDatasets() {
        // given
        Long userId = 123L;

        JwtUserDetails user = mock(JwtUserDetails.class);

        // given
        when(user.getId()).thenReturn(userId);
        when(metadataService.getUserDatasets(userId)).thenReturn(List.of());

        // when
        ResponseEntity<List<DatasetMetadataResp>> resp = controller.fetchDatasets(user);

        // then
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody()).isEmpty();

        verify(metadataService).getUserDatasets(userId);
        verifyNoMoreInteractions(metadataService);
    }
}