package com.example.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TestAuth {

    private final ObjectMapper om = new ObjectMapper();
    private final BaseApiIT base;

    public TestAuth(BaseApiIT base) {
        this.base = base;
    }

    public String signupAndSigninGetToken(String username, String email, String password) throws Exception {
        // signup
        base.mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                          {"username":"%s","email":"%s","password":"%s"}
                        """.formatted(username, email, password)))
                .andExpect(status().is2xxSuccessful());

        // signin
        var res = base.mvc.perform(post("/auth/signin")
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