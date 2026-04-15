package com.example.notification.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationCommand {
    private NotificationRecipientType recipientType;
    private Long recipientId;
    private NotificationCategory category;

    private NotificationType type;
    private String title;
    private String content;

    // The source to tell who is sending the notification
    private NotificationSourceType sourceType;
    private Long sourceId;

    // The target to tell which business is receiving the notification.
    private NotificationTargetType targetType;
    private Long targetId;
    private String actionUrl;
}