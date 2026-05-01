package com.example.notification;

import com.example.notification.domain.NotificationCommand;
import com.example.notification.domain.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(NotificationCommand command) {
        if (command == null) {
            return;
        }
        applicationEventPublisher.publishEvent(new NotificationEvent(command));
    }
}
