package com.example.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.example.auth.domain.user.User;
import com.example.auth.domain.user.UserStatus;
import com.example.auth.infra.jpa.UserRepo;
import com.example.common.dataset.domain.DatasetMetadata;
import com.example.common.dataset.infra.mongo.DatasetMetadataRepo;
import com.example.common.dataset.infra.mongo.DatasetRecordRepo;
import com.example.upload.domain.DatasetStatus;

public class DeleteDatasetApiIT extends BaseApiIT {
  private static final String USERNAME = "username";
  private static final String EMAIL = "username@test.com";
  private static final String PASSWORD = "password";
  private static final String DATASET_NAME = "STOCKS";
  private static final String CSV = """
      recordDate,symbol,price
      2025-12-01,AAPL,180
      2025-12-02,AAPL,182
      2025-12-01,MSFT,310
      """;
  @Autowired
  DatasetMetadataRepo metadataRepo; // Mongo

  @Autowired
  DatasetRecordRepo recordRepo; // Mongo

  @Autowired
  UserRepo userRepo;

  @Test
  void deleteDatasetHappyPath() throws Exception {

    // 1) signup + signin
    String token = signupAndSigninGetToken(USERNAME, EMAIL, PASSWORD);
    User u = userRepo.findByEmail(EMAIL).orElseThrow();
    Long userId = u.getUserId();
    assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVE);

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "data.csv",
        "text/csv",
        CSV.getBytes(StandardCharsets.UTF_8));

    MockMultipartFile datasetPart = new MockMultipartFile(
        "dataset", "", MediaType.APPLICATION_JSON_VALUE,
        ("""
              {
                "datasetName":"stocks",
                "recordDateColumnName":"recordDate",
                "recordDateColumnFormat":"yyyy-MM-dd",
                "recordSymbolColumnName":"symbol",
                "newDataset": true
              }
            """.getBytes(StandardCharsets.UTF_8)));

    mvc.perform(multipart("/upload/uploadCsv")
        .file(file)
        .file(datasetPart)
        .header("Authorization", bearer(token)))
        .andExpect(status().is2xxSuccessful());

    DatasetMetadata meta = metadataRepo
        .findByUserIdAndDatasetName(userId, DATASET_NAME)
        .orElseThrow();

    assertThat(meta.getDatasetName()).isEqualTo(DATASET_NAME);
    assertThat(meta.getUserId()).isEqualTo(userId);
    assertThat(meta.getStatus()).isEqualTo(DatasetStatus.ACTIVE);
    // 4) delete
    mvc.perform(delete("/upload/dataset/{name}", DATASET_NAME)
        .header("Authorization", bearer(token)))
        .andExpect(status().isNoContent());
  }
}