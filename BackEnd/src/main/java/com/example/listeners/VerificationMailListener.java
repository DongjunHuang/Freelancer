package com.example.listeners;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The listener for the finish of the database transaction, then send email to
 * the target.
 */
@Component
@RequiredArgsConstructor
@Data
public class VerificationMailListener {
    private static final Logger logger = LoggerFactory.getLogger(VerificationMailListener.class);

    @Value("${app.frontendBaseUrl}")
    private String feBaseUrl;

    @Value("${app.sendingEmail}")
    private String sendingEmail;

    private final JavaMailSender mail;

    /**
     * Listner the finish of the transanction.
     * 
     * @param event the event to be listened.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(VerificationCreatedEvent event) {
        logger.info("Sending email to email address {} with token {}", event.email(), event.token());
        this.sendVerificationMail(event.email(), event.token());
    }

    /**
     * The action to send email to targeted email address.
     * 
     * @param email the target email address.
     * @param token the token to be clicked for verification.
     */
    public void sendVerificationMail(String email, String token) {
        String link = feBaseUrl + "/verify?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        var msg = new SimpleMailMessage();
        msg.setFrom(sendingEmail);
        msg.setTo(email);
        msg.setSubject("Verify your email");
        msg.setText("""
                Welcome to Data Report Freelancer!

                Please verify your email by clicking the link below (valid for 2 hours):
                %s

                If you did not sign up, please ignore this email.
                """.formatted(link));
        mail.send(msg);
    }
}