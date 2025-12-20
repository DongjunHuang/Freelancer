package com.example.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AppendCsvApiIT extends BaseApiIT {

    @Test
    @Disabled("Not ready yet")
    void appendCsvHappyPath() throws Exception {
        String token = signupAndSigninGetToken("u2", "u2@test.com", "Passw0rd!");

        uploadNewDatasetCsv(token, "stocks", """
                recordDate,symbol,price
                2025-12-01,AAPL,180
                """);

        MockMultipartFile file = new MockMultipartFile(
                "file", "append.csv", "text/csv",
                """
                          recordDate,symbol,price
                          2025-12-02,AAPL,182
                        """.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile datasetPart = new MockMultipartFile(
                "dataset", "", MediaType.APPLICATION_JSON_VALUE,
                """
                          {"datasetName":"stocks"}
                        """.getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/upload/append")
                .file(file)
                .file(datasetPart)
                .header("Authorization", bearer(token)))
                .andExpect(status().is2xxSuccessful());
    }

    private void uploadNewDatasetCsv(String token, String datasetName, String csv) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile datasetPart = new MockMultipartFile(
                "dataset", "", MediaType.APPLICATION_JSON_VALUE,
                """
                          {"datasetName":"%s"}
                        """.formatted(datasetName).getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/upload/new")
                .file(file)
                .file(datasetPart)
                .header("Authorization", bearer(token)))
                .andExpect(status().is2xxSuccessful());
    }
}