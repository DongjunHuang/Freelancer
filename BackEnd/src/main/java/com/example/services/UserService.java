package com.example.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.controllers.AuthController;
import com.example.listeners.VerificationCreatedEvent;
import com.example.repos.MailToken;
import com.example.repos.MailTokenRepo;
import com.example.repos.User;
import com.example.repos.UserRepo;
import com.example.repos.UserStatus;
import com.example.requests.SignupReq;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String DEFAULT_ROLE = "USER";
    private final int TTL_Hours = 1;

    // The user repo in charge of user database
    private final UserRepo userRepo;    
    private final MailTokenRepo mailTokenRepo;    
    
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void signup(SignupReq req) {
        userRepo.findByEmail(req.getEmail()).ifPresent(u -> { throw new IllegalArgumentException("Email taken"); });
        userRepo.findByUsername(req.getUsername()).ifPresent(u -> { throw new IllegalArgumentException("Account taken"); });
        
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
        // Prepare the mail token to the user
        String tokenString = UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphanumeric(32);
        // TODO: the token might be changed for the user to enter 6 digits code
        MailToken token = MailToken.builder()
                                   .userId(user.getUserId())
                                   .email(user.getEmail())
                                   .username(user.getUsername())
                                   .token(tokenString)
                                   .expiresAt(LocalDateTime.now().plusHours(TTL_Hours))
                                   .build();
        mailTokenRepo.save(token);  
        
        // Publish the event for email verification
        publisher.publishEvent(new VerificationCreatedEvent(user.getUserId(), user.getEmail(), token.getToken()));
    }

 
    @Transactional
    public void validateEmail(String token) {
        // TODO: should change to specific exception.
        MailToken tok = mailTokenRepo.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        
        if (tok.isUsed() || tok.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.info("The token is expired.");
            throw new IllegalStateException("Token expired/used");
        }
        
        User user = userRepo.findById(tok.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
        
        tok.setUsed(true);
        mailTokenRepo.save(tok);
    }

    @Transactional
    public void resendEmail(String email)  throws Exception {
        // step1: Remove the old one first
        mailTokenRepo.deleteByEmail(email);

        // step2: check if the user is pending
        User user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getStatus() != UserStatus.PENDING) {
            // TODO : add specific Exception type
            throw new IllegalArgumentException("The user is not pending");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        // step3: issue and save the new generated one
        String tokenString = UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphanumeric(32);
        MailToken token = MailToken.builder()
                                .userId(user.getUserId())
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .token(tokenString)
                                .expiresAt(LocalDateTime.now().plusHours(TTL_Hours))
                                .build();

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