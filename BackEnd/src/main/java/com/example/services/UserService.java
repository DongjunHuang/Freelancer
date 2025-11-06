package com.example.services;

import java.lang.foreign.Linker.Option;
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
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setStatus(UserStatus.PENDING);
        user.setPublicId(UUID.randomUUID().toString());
        userRepo.save(user);
        // Prepare the mail token to the user
        
        MailToken token = new MailToken();
        token.setUserId(user.getUserId());
        token.setEmail(user.getEmail());
        token.setUsername(user.getUsername());
        token.setToken(UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphanumeric(32));
        token.setExpiresAt(LocalDateTime.now().plusHours(TTL_Hours));
        mailTokenRepo.save(token);  
        
        // Publish the event for email verification
        publisher.publishEvent(new VerificationCreatedEvent(user.getUserId(), user.getEmail(), token.getToken()));
    }

 
    @Transactional
    public void validateEmail(String token) {
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
    public void resendEmail(String email) {
        // step1: Remove the old one first
        mailTokenRepo.deleteByEmail(email);

        // step2: check if the user is pending
        User user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalArgumentException("The user is not pending");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        // step3: issue and save the new generated one
        MailToken token = new MailToken();
        token.setUserId(user.getUserId());
        token.setEmail(user.getEmail());
        token.setUsername(user.getUsername());
        token.setToken(UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphanumeric(32));
        token.setExpiresAt(LocalDateTime.now().plusHours(TTL_Hours));
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
                // X-Forwarded-For 可能是多段，取第一个
                return v.split(",")[0].trim();
            }
        }
    return req.getRemoteAddr();
}
}