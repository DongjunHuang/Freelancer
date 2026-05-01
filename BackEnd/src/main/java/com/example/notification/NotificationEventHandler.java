package com.example.notification;

import com.example.notification.app.NotificationService;
import com.example.notification.domain.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;

    @Async("notificationExecutor")
    @EventListener
    public void handle(NotificationEvent event) {
        try {
            notificationService.createNotification(event.command());
        } catch (Exception e) {
            log.error("Failed to create notification. command={}", event.command(), e);
        }
    }
}