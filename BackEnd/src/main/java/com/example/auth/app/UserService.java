package com.example.auth.app;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth.domain.MailToken;
import com.example.auth.domain.SignupReq;
import com.example.auth.domain.User;
import com.example.auth.domain.UserStatus;
import com.example.auth.domain.VerificationCreatedEvent;
import com.example.auth.infra.jpa.MailTokenRepo;
import com.example.auth.infra.jpa.UserRepo;
import com.example.exception.BusinessRuleException;
import com.example.exception.ConflictException;
import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.utils.TokenUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String DEFAULT_ROLE = "USER";
    private final int TTL_Hours = 1;

    // The user repo in charge of user database
    private final UserRepo userRepo;
    private final MailTokenRepo mailTokenRepo;

    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void signup(SignupReq req) {
        userRepo.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new ConflictException(ErrorCode.EMAIL_USED);
        });
        userRepo.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new ConflictException(ErrorCode.USERNAME_USED);
        });

        // Pending waiting for user to verify the email
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .status(UserStatus.PENDING)
                .publicId(UUID.randomUUID().toString())
                .roles(DEFAULT_ROLE)
                .build();
        userRepo.save(user);
        String tokenString = TokenUtils.getRefreshToken();

        MailToken token = mailTokenRepo.findByUserId(user.getUserId())
                .map(existing -> {
                    existing.setToken(tokenString);
                    existing.setExpiresAt(LocalDateTime.now().plusHours(TTL_Hours));
                    existing.setUsed(false);
                    existing.setCreatedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> MailToken.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .token(tokenString)
                        .expiresAt(LocalDateTime.now().plusHours(TTL_Hours))
                        .used(false)
                        .build());
        mailTokenRepo.save(token);

        // Publish the event for email verification
        publisher.publishEvent(new VerificationCreatedEvent(user.getUserId(), user.getEmail(), token.getToken()));
    }

    @Transactional
    public void validateEmail(String token) {
        // TODO: should change to specific exception.
        MailToken tok = mailTokenRepo.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.TOKEN_INVALID));

        if (tok.isUsed() || tok.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepo.findById(tok.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        tok.setUsed(true);
        mailTokenRepo.save(tok);
    }

    @Transactional
    public void resendEmail(String username) {
        // step1: Remove the old one first
        mailTokenRepo.deleteByUsername(username);

        // step2: check if the user is pending
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.USER_IS_NOT_PENDING);
        }

        // step3: issue and save the new generated one
        String tokenString = TokenUtils.getRefreshToken();
        MailToken token = mailTokenRepo.findByUserId(user.getUserId())
                .map(existing -> {
                    existing.setToken(tokenString);
                    existing.setExpiresAt(LocalDateTime.now().plusHours(TTL_Hours));
                    existing.setUsed(false);
                    existing.setCreatedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> MailToken.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .token(tokenString)
                        .expiresAt(LocalDateTime.now().plusHours(TTL_Hours))
                        .used(false)
                        .build());
        mailTokenRepo.save(token);

        // step4: resend the email
        publisher.publishEvent(new VerificationCreatedEvent(user.getUserId(), user.getEmail(), token.getToken()));
    }

    public static String clientIp(HttpServletRequest req) {
        String[] headers = {
                "CF-Connecting-IP",
                "X-Forwarded-For",
                "X-Real-IP"
        };
        for (String h : headers) {
            String v = req.getHeader(h);
            if (v != null && !v.isBlank()) {
                return v.split(",")[0].trim();
            }
        }
        return req.getRemoteAddr();
    }
}