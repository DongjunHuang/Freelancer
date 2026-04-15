package com.example.notification.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationSummaryResponse {
    private long unreadCount;
    private List<NotificationItem> items;
}