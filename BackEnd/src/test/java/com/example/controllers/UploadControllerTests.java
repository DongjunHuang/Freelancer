package com.example.controllers;

import com.example.models.DataProps;
import com.example.repos.DatasetMetadata;
import com.example.repos.User;
import com.example.requests.DatasetMetadataResp;
import com.example.requests.DatasetReq;
import com.example.security.JwtUserDetails;
import com.example.services.MetadataService;
import com.example.services.UploadService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        ResponseEntity<?> resp = controller.uploadWithNewDataset(
                null,
                datasetReq,
                authentication);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body.get("error")).isEqualTo("The file is empty");

        verifyNoInteractions(uploadService);
    }

    @Test
    void testUploadWithNewDatasetWhenFileIsEmpty() {
        when(file.isEmpty()).thenReturn(true);

        ResponseEntity<?> resp = controller.uploadWithNewDataset(
                file,
                datasetReq,
                authentication);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body.get("error")).isEqualTo("The file is empty");

        verifyNoInteractions(uploadService);
    }

    @Test
    void testUploadWithNewDatasetShouldReturnBadRequest() {
        when(file.isEmpty()).thenReturn(false);

        ResponseEntity<?> resp = controller.uploadWithNewDataset(
                file,
                null,
                authentication);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body.get("error")).isEqualTo("table name is empty.");

        verifyNoInteractions(uploadService);
    }

    @Test
    void testUploadWithNewDatasetValid() throws Exception {
        String userName = "1234";
        long userId = 123L;

        User us = User.builder().username(userName).userId(userId).build();
        JwtUserDetails user = new JwtUserDetails(us, null);
        when(file.isEmpty()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(uploadService.appendRecords(any(MultipartFile.class), any(DataProps.class))).thenReturn(42L);

        ResponseEntity<?> resp = controller.uploadWithNewDataset(
                file,
                datasetReq,
                authentication);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body.get("Result")).isEqualTo("Success");

        ArgumentCaptor<DataProps> propsCaptor = ArgumentCaptor.forClass(DataProps.class);
        verify(uploadService).appendRecords(eq(file), propsCaptor.capture());
        DataProps props = propsCaptor.getValue();

        assertThat(props.getUserId()).isEqualTo(userId);
        assertThat(props.getDatasetName()).isEqualTo("my_table");
        assertThat(props.getRecordDateColumnName()).isEqualTo("DATE");
        assertThat(props.getRecordSymbolColumnName()).isEqualTo("SYMBOL");
        assertThat(props.isNewDataset()).isTrue();
        assertThat(props.getBatchId()).isNotNull();
        assertThat(isValidUUID(props.getBatchId())).isTrue();

        // promoteStagedToCurrent
        verify(uploadService).promoteStagedToCurrent("my_table", 123L, 42L);
    }

    @Test
    void testUploadWithNewDatasetWhenServiceThrows() throws Exception {
        String userName = "1234";
        long userId = 123L;

        User us = User.builder().username(userName).userId(userId).build();
        JwtUserDetails user = new JwtUserDetails(us, null);
        when(file.isEmpty()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(uploadService.appendRecords(any(MultipartFile.class), any(DataProps.class)))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> resp = controller.uploadWithNewDataset(
                file,
                datasetReq,
                authentication);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body.get("Result")).isEqualTo("Success");

        verify(uploadService, never()).promoteStagedToCurrent(anyString(), anyLong(), anyLong());
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
        String userName = "1234";
        long userId = 123L;

        User us = User.builder().username(userName).userId(userId).build();
        JwtUserDetails user = new JwtUserDetails(us, null);
        // given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(userId);

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
        ResponseEntity<List<DatasetMetadataResp>> resp = controller.fetchDatasets(authentication);

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
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(userId);
        when(metadataService.getUserDatasets(userId)).thenReturn(List.of());

        // when
        ResponseEntity<List<DatasetMetadataResp>> resp = controller.fetchDatasets(authentication);

        // then
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody()).isEmpty();

        verify(metadataService).getUserDatasets(userId);
        verifyNoMoreInteractions(metadataService);
    }
}