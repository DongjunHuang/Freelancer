package com.example.functional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.security.CustomUserDetailsService;
import com.example.security.JsonAccessDeniedHandler;
import com.example.security.JsonAuthEntryPoint;
import com.example.security.JwtRequestFilter;
import com.example.security.SecurityConfig;

@WebMvcTest(TestSecurityController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
// TODO: add more functional tests
public class SecurityConfigWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JsonAuthEntryPoint jsonAuthEntryPoint;

    @MockitoBean
    private JsonAccessDeniedHandler jsonAccessDeniedHandler;

    @Test
    void testAuthApishouldAlBePermitted() throws Exception {
        mockMvc.perform(get("/auth/ping")).andExpect(status().isOk());
    }

    @Test
    void testOtherApishouldAlBePermitted() throws Exception {
        mockMvc.perform(get("/secure/ping")).andExpect(status().isOk());
    }
}