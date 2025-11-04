package com.example.functiontest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class TestEmailService {
    private final JavaMailSender mailSender;
    @Value("${app.mailTo}")
    private String toAddress;

    @Value("${app.mailFrom}")
    private String fromAddress;

    public TestEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationMail(String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toAddress);
        msg.setSubject("请验证你的邮件");
        msg.setText("请点击以下链接验证: https://your-frontend/verify?token=" + token);

        mailSender.send(msg);
    }
}