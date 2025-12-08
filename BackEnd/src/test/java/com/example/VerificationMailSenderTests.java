package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import static org.mockito.Mockito.verify;
import com.example.listeners.VerificationMailListener;

public class VerificationMailSenderTests {

    @InjectMocks
    private VerificationMailListener listener;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        listener.setSendingEmail("noreply@test.com");
        listener.setFeBaseUrl("http://frontend");
        ;
    }

    @Test
    void sendVerificationMail_shouldBuildCorrectMessageAndSend() {
        String email = "user@example.com";
        String token = "abc123";

        // when
        listener.sendVerificationMail(email, token);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assert msg.getTo()[0].equals(email);
        assert msg.getFrom().equals("noreply@test.com");
        assert msg.getSubject().equals("Verify your email");
        assert msg.getText().contains("http://frontend/verify?token=abc123");
    }
}