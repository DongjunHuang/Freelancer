package com.example.notification.domain;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationItem {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String actionUrl;
    private boolean isRead;
    private Instant readAt;
    private Instant createdAt;

    public static NotificationItem toNotificationDto(Notification n) {
        return NotificationItem.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .content(n.getContent())
                .actionUrl(n.getActionUrl())
                .isRead(n.isRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}