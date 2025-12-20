package com.example.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseApiIT {

        @Autowired
        protected MockMvc mvc;
        @Autowired
        protected ObjectMapper om;

        protected String bearer(String token) {
                return "Bearer " + token;
        }

        protected String signupAndSigninGetToken(String username, String email, String password) throws Exception {
                // 1) signup
                mvc.perform(post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                  {"username":"%s","email":"%s","password":"%s"}
                                                """.formatted(username, email, password)))
                                .andExpect(status().is2xxSuccessful());

                // 2) signin
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
}