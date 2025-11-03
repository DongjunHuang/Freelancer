package com.example.listeners;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerificationMailListener {
    @Value("${app.frontendBaseUrl}") 
    private String feBaseUrl;

    @Value("${app.mailFrom}") 
    private String mailFrom;
    
    private final JavaMailSender mail;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(VerificationCreatedEvent event) {
        this.sendVerificationMail(event.email(), event.token());
    }

    private void sendVerificationMail(String email, String token) {
        var link = feBaseUrl + "/verify?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        var msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(email);
        msg.setSubject("Verify your email");
        msg.setText("""
        Welcome to YourBrand!

        Please verify your email by clicking the link below (valid for 1 hours):
        %s

        If you did not sign up, please ignore this email.
        """.formatted(link));
        mail.send(msg);
    }
}