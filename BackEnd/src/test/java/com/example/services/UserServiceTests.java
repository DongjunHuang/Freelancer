package com.example.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.exception.BusinessRuleException;
import com.example.exception.ConflictException;
import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.listeners.VerificationCreatedEvent;
import com.example.repos.MailToken;
import com.example.repos.MailTokenRepo;
import com.example.repos.User;
import com.example.repos.UserRepo;
import com.example.repos.UserStatus;
import com.example.requests.SignupReq;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

        @InjectMocks
        private UserService userService;

        @Mock
        private MailTokenRepo mailTokenRepo;

        @Mock
        private UserRepo userRepo;

        @Mock
        private ApplicationEventPublisher publisher;

        @Mock
        private PasswordEncoder encoder;

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
                                .hasMessageContaining("User not found");

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
                                .hasMessageContaining("Email already registered");

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
                                .hasMessageContaining("Username already taken");

                verify(userRepo, never()).save(any());
                verify(mailTokenRepo, never()).save(any());
                verify(publisher, never()).publishEvent(any());
        }
}
