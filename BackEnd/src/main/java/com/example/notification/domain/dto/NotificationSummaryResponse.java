package com.example.notification.domain.dto;

import com.example.notification.domain.NotificationItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationSummaryResponse {
    private long unreadCount;
    private List<NotificationItem> items;
}