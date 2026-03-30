package com.example.services;

import com.example.auth.domain.user.*;
import com.example.auth.infra.jpa.RefreshTokenRepo;
import com.example.security.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.auth.app.user.UserService;
import com.example.auth.infra.jpa.MailTokenRepo;
import com.example.auth.infra.jpa.UserRepo;
import com.example.exception.types.BusinessRuleException;
import com.example.exception.types.ConflictException;
import com.example.exception.ErrorCode;
import com.example.exception.types.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Mock
    private Environment env;

    @Mock
    private MailTokenRepo mailTokenRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private RefreshTokenRepo refreshTokenRepo;

    @Test
    void testValidateEmailInvalidToken() {
        when(mailTokenRepo.findByToken("abc")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.validateEmail("abc"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(ErrorCode.TOKEN_INVALID.getMessage());

        verify(mailTokenRepo).findByToken("abc");
        verifyNoMoreInteractions(mailTokenRepo, userRepo);
    }

    @Test
    void testValidateEmailExpiredToken() {
        MailToken token = MailToken.builder()
                .token("abc")
                .userId(1L)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(mailTokenRepo.findByToken("abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> userService.validateEmail("abc"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(ErrorCode.TOKEN_EXPIRED.getMessage());

        verify(mailTokenRepo).findByToken("abc");
        verifyNoMoreInteractions(mailTokenRepo, userRepo);
    }

    @Test
    void testValidateEmailTokenUsed() {
        MailToken token = MailToken.builder()
                .token("abc")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(true)
                .build();

        when(mailTokenRepo.findByToken("abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> userService.validateEmail("abc"))
                .isInstanceOf(BusinessRuleException.class);

        verify(mailTokenRepo).findByToken("abc");
    }

    @Test
    void testValidateEmailUserNotFound() {
        MailToken token = MailToken.builder()
                .token("abc")
                .userId(99L)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(mailTokenRepo.findByToken("abc")).thenReturn(Optional.of(token));
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.validateEmail("abc"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(mailTokenRepo).findByToken("abc");
        verify(userRepo).findById(99L);
    }

    @Test
    void testValidateEmailValidToken() {
        MailToken token = MailToken.builder()
                .token("abc")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        User user = User.builder().userId(1L).status(UserStatus.PENDING).build();

        when(mailTokenRepo.findByToken("abc")).thenReturn(Optional.of(token));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        userService.validateEmail("abc");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

        assertThat(token.isUsed()).isTrue();

        verify(userRepo).save(user);
        verify(mailTokenRepo).save(token);
    }

    @Test
    void testResendEmailShouldGenerateNewTokenAndPublishEvent() {
        String username = "john";

        User user = User.builder().userId(100L).username(username).status(UserStatus.PENDING).build();
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));

        LocalDateTime before = LocalDateTime.now();

        userService.resendEmail(username);

        LocalDateTime after = LocalDateTime.now();

        // The resend should not activate user
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);

        ArgumentCaptor<MailToken> tokenCaptor = ArgumentCaptor.forClass(MailToken.class);
        verify(mailTokenRepo).save(tokenCaptor.capture());

        MailToken saved = tokenCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(100L);
        assertThat(saved.getUsername()).isEqualTo(username);
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(before);
        assertThat(saved.getExpiresAt()).isBefore(after.plusHours(5));

        ArgumentCaptor<VerificationCreatedEvent> eventCaptor = ArgumentCaptor
                .forClass(VerificationCreatedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());

        VerificationCreatedEvent evt = eventCaptor.getValue();
        assertThat(evt.userId()).isEqualTo(100L);
        assertThat(evt.token()).isEqualTo(saved.getToken());
    }

    @Test
    void testResendEmailUserNotFound() {
        String username = "John";

        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resendEmail(username))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(mailTokenRepo, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void testResendEmailWhenUserIsNotPending() {
        String username = "John";

        User user = User.builder()
                .userId(200L)
                .username("tom")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.resendEmail(username))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage(ErrorCode.USER_IS_NOT_PENDING.getMessage());

        verify(userRepo, never()).save(any());
        verify(mailTokenRepo, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void testSignupValidEmailAndUsername() {
        // given
        SignupReq req = new SignupReq();
        req.setEmail("test@example.com");
        req.setUsername("john");
        req.setPassword("raw-pwd");

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());
        when(encoder.encode("raw-pwd")).thenReturn("encoded-pwd");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(1L);
            return u;
        });

        ArgumentCaptor<MailToken> tokenCaptor = ArgumentCaptor.forClass(MailToken.class);
        ArgumentCaptor<VerificationCreatedEvent> eventCaptor = ArgumentCaptor
                .forClass(VerificationCreatedEvent.class);

        // when
        userService.signup(req);

        // then
        verify(userRepo).findByEmail("test@example.com");
        verify(userRepo).findByUsername("john");

        verify(userRepo).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("john");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-pwd");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(savedUser.getPublicId()).isNotBlank();

        verify(mailTokenRepo).save(tokenCaptor.capture());
        MailToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUserId()).isEqualTo(1L);
        assertThat(savedToken.getEmail()).isEqualTo("test@example.com");
        assertThat(savedToken.getUsername()).isEqualTo("john");
        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now().minusMinutes(1));

        verify(publisher).publishEvent(eventCaptor.capture());
        VerificationCreatedEvent evt = eventCaptor.getValue();
        assertThat(evt.userId()).isEqualTo(1L);
        assertThat(evt.email()).isEqualTo("test@example.com");
        assertThat(evt.token()).isEqualTo(savedToken.getToken());
    }

    @Test
    void testSignupEmailIsTaken() {
        // given
        SignupReq req = new SignupReq();
        req.setEmail("test@example.com");
        req.setUsername("john");
        req.setPassword("raw-pwd");

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(User.builder().build()));

        // when / then
        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ErrorCode.EMAIL_USED.getMessage());

        verify(userRepo, never()).findByUsername(anyString());
        verify(userRepo, never()).save(any());
        verify(mailTokenRepo, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void testSignupWhenUsernameIsTaken() {
        // given
        SignupReq req = new SignupReq();
        req.setEmail("test@example.com");
        req.setUsername("john");
        req.setPassword("raw-pwd");

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepo.findByUsername("john")).thenReturn(Optional.of(User.builder().build()));

        // when / then
        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ErrorCode.USERNAME_USED.getMessage());

        verify(userRepo, never()).save(any());
        verify(mailTokenRepo, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void signInShouldReturnResponseWithCookiesAndSecureFalseWhenNotProd() {
        String username = "alice";
        String email = "alice@example.com";
        String ipAddress = "127.0.0.1";

        when(jwtService.generateRefreshToken(username, email)).thenReturn("refresh-token-123");
        when(jwtService.generateSignedDeviceId()).thenReturn("device-id-456");
        when(jwtService.generateAccessToken(username, email)).thenReturn("access-token-789");
        when(env.getActiveProfiles()).thenReturn(new String[]{"dev"});

        ResponseEntity<SigninResp> response = userService.signin(username, email, ipAddress);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        SigninResp body = response.getBody();
        assertEquals("access-token-789", body.getAccessToken());
        assertNotNull(body.getUser());
        assertEquals(username, body.getUser().getUsername());
        assertEquals(email, body.getUser().getEmail());

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(2, cookies.size());

        String refreshCookie = cookies.get(0);
        String deviceCookie = cookies.get(1);

        assertTrue(refreshCookie.contains("refreshToken=refresh-token-123"));
        assertTrue(refreshCookie.contains("HttpOnly"));
        assertTrue(refreshCookie.contains("Path=/auth"));
        assertTrue(refreshCookie.contains("SameSite=Strict"));
        assertFalse(refreshCookie.contains("Secure"));

        assertTrue(deviceCookie.contains("deviceId=device-id-456"));
        assertTrue(deviceCookie.contains("HttpOnly"));
        assertTrue(deviceCookie.contains("Path=/"));
        assertTrue(deviceCookie.contains("SameSite=Strict"));
        assertFalse(deviceCookie.contains("Secure"));

        verify(jwtService).generateRefreshToken(username, email);
        verify(jwtService).generateSignedDeviceId();
        verify(jwtService).generateAccessToken(username, email);
        verify(env).getActiveProfiles();
    }

    @Test
    void signInShouldReturnSecureCookiesWhenProdProfileIsActive() {
        String username = "bob";
        String email = "bob@example.com";
        String ipAddress = "10.0.0.1";

        when(jwtService.generateRefreshToken(username, email)).thenReturn("refresh-prod");
        when(jwtService.generateSignedDeviceId()).thenReturn("device-prod");
        when(jwtService.generateAccessToken(username, email)).thenReturn("access-prod");
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        ResponseEntity<SigninResp> response = userService.signin(username, email, ipAddress);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("access-prod", response.getBody().getAccessToken());
        assertEquals(username, response.getBody().getUser().getUsername());
        assertEquals(email, response.getBody().getUser().getEmail());

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(2, cookies.size());

        String refreshCookie = cookies.get(0);
        String deviceCookie = cookies.get(1);

        assertTrue(refreshCookie.contains("refreshToken=refresh-prod"));
        assertTrue(refreshCookie.contains("Secure"));

        assertTrue(deviceCookie.contains("deviceId=device-prod"));
        assertTrue(deviceCookie.contains("Secure"));

        verify(jwtService).generateRefreshToken(username, email);
        verify(jwtService).generateSignedDeviceId();
        verify(jwtService).generateAccessToken(username, email);
        verify(env).getActiveProfiles();
    }

    @Test
    void signInShouldTreatProdAsActiveWhenProdExistsAmongMultipleProfiles() {
        String username = "charlie";
        String email = "charlie@example.com";
        String ipAddress = "192.168.1.10";

        when(jwtService.generateRefreshToken(username, email)).thenReturn("refresh-multi");
        when(jwtService.generateSignedDeviceId()).thenReturn("device-multi");
        when(jwtService.generateAccessToken(username, email)).thenReturn("access-multi");
        when(env.getActiveProfiles()).thenReturn(new String[]{"dev", "prod"});

        ResponseEntity<SigninResp> response = userService.signin(username, email, ipAddress);

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(2, cookies.size());

        assertTrue(cookies.get(0).contains("Secure"));
        assertTrue(cookies.get(1).contains("Secure"));
    }
}
