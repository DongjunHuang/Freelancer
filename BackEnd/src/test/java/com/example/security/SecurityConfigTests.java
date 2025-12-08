package com.example.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

public class SecurityConfigTests {

    private JwtRequestFilter jwtRequestFilter;
    private CustomUserDetailsService customUserDetailsService;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtRequestFilter = mock(JwtRequestFilter.class);
        customUserDetailsService = mock(CustomUserDetailsService.class);
        securityConfig = new SecurityConfig(jwtRequestFilter, customUserDetailsService);
    }

    @Test
    void passwordEncoder_shouldReturnBCryptEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);

        String raw = "password123";
        String encoded = encoder.encode(raw);
        assertThat(encoder.matches(raw, encoded)).isTrue();
    }

    @Test
    void daoAuthenticationProvider_shouldUseCustomUserDetailsServiceAndPasswordEncoder() {
        DaoAuthenticationProvider provider = securityConfig.daoAuthenticationProvider();

        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "pwd";
        String encoded = encoder.encode(raw);
        assertThat(provider).isNotNull();
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
        assertThat(provider).extracting("hideUserNotFoundExceptions"); // 只是访问一下内部字段避免 IDE 警告

        assertThat(encoder.matches(raw, encoded)).isTrue();
    }

    @Test
    void corsConfigurationSource_shouldHaveExpectedSettings() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cfg = source.getCorsConfiguration(req);
        assertThat(cfg).isNotNull();
        assertThat(cfg.getAllowedOrigins())
                .containsExactlyInAnyOrder(
                        "https://freelancer-fvwm.onrender.com",
                        "http://localhost:5173");
        assertThat(cfg.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(cfg.getAllowedHeaders()).containsExactly("*");
        assertThat(cfg.getAllowCredentials()).isTrue();
    }

    @Test
    void authenticationManager_shouldBeObtainedFromConfig() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);

        org.mockito.Mockito.when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager mgr = securityConfig.authenticationManager(authConfig);

        assertThat(mgr).isSameAs(mockManager);
    }
}