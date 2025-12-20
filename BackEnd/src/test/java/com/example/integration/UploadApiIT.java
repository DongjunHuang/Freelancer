package com.example.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadApiIT extends BaseApiIT {

    @Test
    @Disabled("Not ready yet")
    void uploadDatasetHappyPath() throws Exception {
        String token = signupAndSigninGetToken("u1", "u1@test.com", "Passw0rd!");

        String csv = """
                recordDate,symbol,price
                2025-12-01,AAPL,180
                2025-12-02,AAPL,182
                2025-12-01,MSFT,310
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile datasetPart = new MockMultipartFile(
                "dataset", "", MediaType.APPLICATION_JSON_VALUE,
                """
                          {"datasetName":"stocks"}
                        """.getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/upload/new")
                .file(file)
                .file(datasetPart)
                .header("Authorization", bearer(token)))
                .andExpect(status().is2xxSuccessful());
    }
}