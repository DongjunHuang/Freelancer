package com.example.integration;

import com.example.auth.infra.jpa.MailTokenRepo;
import com.example.auth.infra.jpa.UserRepo;
import com.example.dataset.infra.mongo.DatasetMetadataRepo;
import com.example.dataset.infra.mongo.DatasetRecordRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestMailConfig.class)
public abstract class BaseApiIT {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper om;

    @Autowired
    MailTokenRepo mailTokenRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DatasetMetadataRepo metadataRepo;

    @Autowired
    DatasetRecordRepo recordRepo;

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String signupAndSignInGetToken(String username, String email, String password) throws Exception {
        // 1) signup
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {"username":"%s","email":"%s","password":"%s"}
                                """.formatted(username, email, password)))
                .andExpect(status().is2xxSuccessful());

        // 2) write to mail token
        var tok = mailTokenRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("No mail token created for " + email));

        // 3). verify the token
        mvc.perform(get("/auth/verify").param("token", tok.getToken()))
                .andExpect(status().is2xxSuccessful());

        // 4). signin
        var res = mvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = om.readTree(res);
        return root.path("accessToken").asText();
    }

    @AfterEach
    void cleanup() {
        recordRepo.deleteAll();
        metadataRepo.deleteAll();
        userRepo.deleteAll();
    }
}