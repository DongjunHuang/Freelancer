package com.example.functiontest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestEmailService {
    @Value("${app.mailTo}")
    private String toAddress;

    @Value("${app.mailFrom}")
    private String fromAddress;
    
    private final JavaMailSender mailSender;
    
    public void sendVerificationMail(String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toAddress);
        msg.setSubject("Please verify your email");
        msg.setText("Please click below to verify: https://your-frontend/verify?token=" + token);

        mailSender.send(msg);
    }
}