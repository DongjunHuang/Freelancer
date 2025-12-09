package com.example.controllers;

import com.example.repos.RefreshToken;
import com.example.requests.SigninReq;
import com.example.security.JwtUserDetails;
import com.example.security.SecretService;
import com.example.security.TokenInfo;
import com.example.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerSigninTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    SecretService jwtService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    Environment env;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    AuthController controller;

    @Test
    void testSigninValid() {
        // Arrange
        SigninReq req = new SigninReq();
        req.setUsername("john");
        req.setPassword("secret");

        JwtUserDetails principal = mock(JwtUserDetails.class);
        when(principal.getUsername()).thenReturn("john");
        when(principal.getEmail()).thenReturn("john@example.com");
        when(principal.getAuthorities()).thenReturn(List.of());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        when(jwtService.generateRefreshToken("john", "john@example.com"))
                .thenReturn("refresh-token-123");
        when(jwtService.generateSignedDeviceId())
                .thenReturn("device-id-xyz");
        when(jwtService.generateAccessToken("john", "john@example.com"))
                .thenReturn("access-token-abc");

        when(env.getActiveProfiles()).thenReturn(new String[] { "dev" });

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // === Act ===
        ResponseEntity<?> resp = controller.signin(req, request);

        // === Assert ===
        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        assertThat(resp.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();

        assertThat(body).containsKeys("accessToken", "user");
        assertThat(body.get("accessToken")).isEqualTo("access-token-abc");

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) body.get("user");
        assertThat(userMap.get("username")).isEqualTo("john");
        assertThat(userMap.get("email")).isEqualTo("john@example.com");

        // Cookie related
        List<String> cookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        assertThat(cookies).hasSize(2);

        // refreshToken cookie
        assertThat(cookies.get(0)).contains("refreshToken=refresh-token-123");
        assertThat(cookies.get(0)).contains("HttpOnly");
        assertThat(cookies.get(0)).contains("Max-Age=604800");
        assertThat(cookies.get(0)).contains("Path=/auth");
        assertThat(cookies.get(0)).contains("SameSite=Strict");

        // deviceid cookie
        assertThat(cookies.get(1)).contains("deviceid=device-id-xyz");
        assertThat(cookies.get(1)).contains("HttpOnly");
        assertThat(cookies.get(1)).contains("Path=/");
        assertThat(cookies.get(1)).contains("SameSite=Strict");

        ArgumentCaptor<LocalDateTime> expiryCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(refreshTokenService).createAndSaveRefreshToken(
                eq("john"),
                eq("refresh-token-123"),
                eq("device-id-xyz"),
                eq("127.0.0.1"),
                expiryCaptor.capture());

    }

    @Test
    void testSigninBadCredentials() {
        // === Arrange ===
        SigninReq req = new SigninReq();
        req.setUsername("john");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // === Act ===
        ResponseEntity<?> resp = controller.signin(req, request);

        // === Assert ===
        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        assertThat(resp.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body.get("error")).isEqualTo("Invalid credentials");

        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void testSignoutWithValidAccessToken() {
        // given
        String accessToken = "access-token";
        String authHeader = "Bearer " + accessToken;

        TokenInfo info = new TokenInfo();
        info.setUsername("alice");

        when(jwtService.isValid(accessToken)).thenReturn(true);
        when(jwtService.parse(accessToken)).thenReturn(info);

        // when
        ResponseEntity<Void> resp = controller.signout(authHeader, null, null);

        // then
        assertEquals(HttpStatus.OK, resp.getStatusCode());

        String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("refreshToken="));
        assertTrue(setCookie.toLowerCase().contains("max-age=0"));

        verify(refreshTokenService).revokeByUsernameAndDeviceId("alice", null);
        verifyNoMoreInteractions(refreshTokenService);
    }

    @Test
    void testSignoutWithValidRefreshToken() {
        // given
        String refreshToken = "refresh-token";
        String deviceId = "device-123";
        String username = "bob";
        String authHeader = null;
        LocalDateTime localtime = LocalDateTime.now().plusDays(1);
        when(jwtService.isValid(refreshToken)).thenReturn(true);

        RefreshToken rt = RefreshToken.builder().username(username).expiresAt(localtime).build();

        when(refreshTokenService.findByToken(refreshToken)).thenReturn(Optional.of(rt));

        // when
        ResponseEntity<Void> resp = controller.signout(authHeader, refreshToken, deviceId);

        // then
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("refreshToken="));
        assertTrue(setCookie.toLowerCase().contains("max-age=0"));

        verify(refreshTokenService).revokeByUsernameAndDeviceId("bob", deviceId);
        verifyNoMoreInteractions(refreshTokenService);
    }

    @Test
    void testSignoutWhenNoValidToken() {
        String authHeader = "Bearer invalid-token";
        when(jwtService.isValid("invalid-token")).thenReturn(false);

        String refreshCookie = null;
        String deviceId = null;

        // when
        ResponseEntity<Void> resp = controller.signout(authHeader, refreshCookie, deviceId);

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertFalse(resp.getHeaders().containsKey(HttpHeaders.SET_COOKIE));

        verify(refreshTokenService, never()).revokeByUsernameAndDeviceId(anyString(), any());
        verify(refreshTokenService, never()).findByToken(anyString());
    }

    @Test
    void testSignoutWithExpiredRefreshToken() {
        // given
        String refreshToken = "refresh-token";
        String deviceId = "device-123";
        String username = "bob";
        LocalDateTime localtime = LocalDateTime.now().minusMinutes(1);
        when(jwtService.isValid(refreshToken)).thenReturn(true);

        RefreshToken rt = RefreshToken.builder().username(username).expiresAt(localtime).build();

        when(refreshTokenService.findByToken(refreshToken)).thenReturn(Optional.of(rt));

        // when
        ResponseEntity<Void> resp = controller.signout(null, refreshToken, deviceId);

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        verify(refreshTokenService, never()).revokeByUsernameAndDeviceId(anyString(), anyString());
    }

    @Test
    void refreshAccessToken_whenRefreshTokenIsNull_shouldReturn401() {
        // when
        ResponseEntity<?> resp = controller.refreshAccessToken(null);

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Missing refresh token", resp.getBody());
        verifyNoInteractions(jwtService);
    }

    @Test
    void testRefreshAccessTokenWhenRefreshTokenIsBlank() {
        // when
        ResponseEntity<?> resp = controller.refreshAccessToken("   ");

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Missing refresh token", resp.getBody());
        verifyNoInteractions(jwtService);
    }

    @Test
    void testRefreshAccessTokenWhenRefreshTokenInvalid() {
        // given
        String refreshToken = "invalid-rt";
        when(jwtService.isValid(refreshToken)).thenReturn(false);

        // when
        ResponseEntity<?> resp = controller.refreshAccessToken(refreshToken);

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Invalid refresh token", resp.getBody());
        verify(jwtService).isValid(refreshToken);
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRefreshAccessTokenWhenRefreshTokenValid() {
        // given
        String refreshToken = "valid-rt";
        when(jwtService.isValid(refreshToken)).thenReturn(true);

        TokenInfo info = new TokenInfo();
        info.setUsername("alice");
        info.setEmail("alice@example.com");
        when(jwtService.parse(refreshToken)).thenReturn(info);

        when(jwtService.generateAccessToken("alice", "alice@example.com"))
                .thenReturn("new-access-token");

        // when
        ResponseEntity<?> resp = controller.refreshAccessToken(refreshToken);

        // then
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody() instanceof Map);

        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertEquals("new-access-token", body.get("accessToken"));

        verify(jwtService).isValid(refreshToken);
        verify(jwtService).parse(refreshToken);
        verify(jwtService).generateAccessToken("alice", "alice@example.com");
        verifyNoMoreInteractions(jwtService);
    }
}