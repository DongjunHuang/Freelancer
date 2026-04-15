package com.example.notification.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationRecipientType recipientType;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationTargetType targetType;

    @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    private Long recipientId;
    private String title;
    private String content;
    private Long targetId;
    private String actionUrl;
    private String groupKey;
    private Integer eventCount;
    private boolean isRead;
    private Instant readAt;
    private Instant firstEventAt;
    private Instant lastEventAt;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (firstEventAt == null) {
            firstEventAt = now;
        }

        if (lastEventAt == null) {
            lastEventAt = now;
        }

        if (eventCount == null || eventCount <= 0) {
            eventCount = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}