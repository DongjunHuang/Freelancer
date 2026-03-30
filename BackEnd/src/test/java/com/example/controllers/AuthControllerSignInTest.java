package com.example.controllers;

import com.example.auth.app.user.UserService;
import com.example.auth.domain.user.*;
import com.example.auth.interfaces.UserAuthController;
import com.example.exception.ErrorCode;
import com.example.exception.types.AuthenticationException;
import com.example.security.JwtUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerSignInTest {
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "password123";
    private static final String EMAIL = "alice@example.com";
    private List<GrantedAuthority> AUTHORITIES =  List.of(new SimpleGrantedAuthority("ROLE_USER"));
    private static final String ACCESS_TOKEN = "access-token";
    private static final String IP_ADDRESS = "127.0.0.1";


    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authentication;

    @Spy
    @InjectMocks
    private UserAuthController controller;

    @Test
    void signInShouldReturnResponseWhenUserIsValid() {
        SigninReq req = new SigninReq();
        req.setUsername(USERNAME);
        req.setPassword(PASSWORD);
        User user = User.builder().email(EMAIL).username(USERNAME).status(UserStatus.ACTIVE).build();
        JwtUserDetails principal = JwtUserDetails.builder().user(user).authorities(AUTHORITIES).build();

        SigninResp signinResp = SigninResp.builder()
                .accessToken(ACCESS_TOKEN)
                .user(SigninResp.UserInfo.builder()
                        .username(USERNAME)
                        .email(EMAIL)
                        .build())
                .build();

        ResponseEntity<SigninResp> expectedResponse = ResponseEntity.ok(signinResp);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        doReturn(IP_ADDRESS).when(controller).getClientIp(request);
        when(userService.signin(USERNAME, EMAIL, IP_ADDRESS))
                .thenReturn(expectedResponse);

        ResponseEntity<SigninResp> actualResponse = controller.signin(req, request);

        assertNotNull(actualResponse);
        assertEquals(200, actualResponse.getStatusCode().value());
        assertNotNull(actualResponse.getBody());
        assertEquals(ACCESS_TOKEN, actualResponse.getBody().getAccessToken());
        assertEquals(USERNAME, actualResponse.getBody().getUser().getUsername());
        assertEquals(EMAIL, actualResponse.getBody().getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).signin(USERNAME, EMAIL, IP_ADDRESS);
        verify(controller).getClientIp(request);
    }

    @Test
    void signInShouldThrowAuthenticationExceptionWhenUserStatusIsPending() {
        SigninReq req = new SigninReq();
        req.setUsername(USERNAME);
        req.setPassword(PASSWORD);
        User user = User.builder().email(EMAIL).username(USERNAME).status(UserStatus.PENDING).build();
        JwtUserDetails principal = JwtUserDetails.builder().user(user).authorities(AUTHORITIES).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> controller.signin(req, request)
        );

        assertEquals(ErrorCode.USER_IS_NOT_VERIFIED, ex.getErrorCode());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).signin(anyString(), anyString(), anyString());
        verify(controller, never()).getClientIp(any());
    }

    @Test
    void signInShouldThrowAuthenticationExceptionWhenPrincipalDoesNotHaveRoleUser() {
        SigninReq req = new SigninReq();
        req.setUsername(USERNAME);
        req.setPassword(PASSWORD);
        User user = User.builder().email(EMAIL).username(USERNAME).status(UserStatus.ACTIVE).build();
        JwtUserDetails principal = JwtUserDetails.builder().user(user).authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);

        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> controller.signin(req, request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).signin(anyString(), anyString(), anyString());
        verify(controller, never()).getClientIp(any());
    }

    @Test
    void signout_shouldThrowAuthenticationException_whenUserIsNull() {
        String deviceId = "device-123";

        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> controller.signout(null, deviceId)
        );

        assertEquals(ErrorCode.USER_IS_NOT_VERIFIED, ex.getErrorCode());
        verify(userService, never()).revokeByUsernameAndDeviceId(anyString(), anyString());
    }

    @Test
    void signout_shouldThrowAuthenticationException_whenDeviceIdIsNull() {
        JwtUserDetails user = mock(JwtUserDetails.class);

        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> controller.signout(user, null)
        );

        assertEquals(ErrorCode.USER_IS_NOT_VERIFIED, ex.getErrorCode());
        verify(userService, never()).revokeByUsernameAndDeviceId(anyString(), anyString());
    }

    @Test
    void signOutShouldRevokeTokenAndClearCookiesWhenInputsAreValid() {
        JwtUserDetails user = mock(JwtUserDetails.class);
        when(user.getUsername()).thenReturn("alice");

        String deviceId = "device-123";

        ResponseEntity<Void> response = controller.signout(user, deviceId);

        assertEquals(200, response.getStatusCode().value());

        verify(userService).revokeByUsernameAndDeviceId("alice", "device-123");

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(2, cookies.size());

        String refreshTokenCookie = cookies.get(0);
        String deviceIdCookie = cookies.get(1);

        assertTrue(refreshTokenCookie.contains("refreshToken="));
        assertTrue(refreshTokenCookie.contains("Max-Age=0"));
        assertTrue(refreshTokenCookie.contains("HttpOnly"));
        assertTrue(refreshTokenCookie.contains("Path=/auth"));

        assertTrue(deviceIdCookie.contains("deviceId="));
        assertTrue(deviceIdCookie.contains("Max-Age=0"));
        assertTrue(deviceIdCookie.contains("HttpOnly"));
        assertTrue(deviceIdCookie.contains("Path=/auth"));
    }
}