package com.example.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.example.auth.domain.user.User;
import com.example.auth.domain.user.UserStatus;
import com.example.auth.infra.jpa.UserRepo;
import com.example.common.dataset.domain.DatasetMetadata;
import com.example.common.dataset.infra.mongo.DatasetMetadataRepo;
import com.example.common.dataset.infra.mongo.DatasetRecordRepo;
import com.example.upload.domain.DatasetStatus;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadApiIT extends BaseApiIT {
    private static final String USERNAME = "username";
    private static final String EMAIL = "username@test.com";
    private static final String PASSWORD = "password";
    private static final String DATASET_NAME = "STOCKS";

    // set up the csv content
    private static final String CSV = """
            recordDate,symbol,price
            2025-12-01,AAPL,180
            2025-12-02,AAPL,182
            2025-12-01,MSFT,310
            """;
    private static final String CSV_PRE = """
            recordDate,symbol,price
            2025-12-01,SYM,180
            2025-12-02,AAPL,182
            """;
    @Autowired
    DatasetMetadataRepo metadataRepo;

    @Autowired
    DatasetRecordRepo recordRepo; // Mongo

    @Autowired
    UserRepo userRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * Create a new dataset.
     *
     * @throws Exception
     */
    @Test
    void uploadDatasetHappyPath() throws Exception {
        String token = signupAndSignInGetToken(USERNAME, EMAIL, PASSWORD);
        User u = userRepo.findByEmail(EMAIL).orElseThrow();
        Long userId = u.getUserId();
        assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVE);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                CSV_PRE.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile datasetPart = new MockMultipartFile(
                "dataset",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                ("""
                        {
                          "datasetName":"stocks",
                          "recordDateColumnName":"recordDate",
                          "recordDateColumnFormat":"yyyy-MM-dd",
                          "recordSymbolColumnName":"symbol",
                          "newDataset":true
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

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                CSV.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile datasetPart2 = new MockMultipartFile(
                "dataset", "", MediaType.APPLICATION_JSON_VALUE,
                ("""
                        {
                          "datasetName":"stocks",
                          "newDataset":false
                        }
                        """.getBytes(StandardCharsets.UTF_8)));

        // 2) append
        mvc.perform(multipart("/upload/uploadCsv")
                        .file(file2)
                        .file(datasetPart2)
                        .header("Authorization", bearer(token)))
                .andExpect(status().is2xxSuccessful());
    }
}