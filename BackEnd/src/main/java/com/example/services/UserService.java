package com.example.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.listeners.VerificationCreatedEvent;
import com.example.repos.MailToken;
import com.example.repos.MailTokenRepo;
import com.example.repos.User;
import com.example.repos.UserRepo;
import com.example.repos.UserStatus;
import com.example.requests.SignupReq;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
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
            throw new IllegalStateException("Token expired/used");
        }
        
        User user = userRepo.findById(tok.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
        
        tok.setUsed(true);
        mailTokenRepo.save(tok);
    }
}