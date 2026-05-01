package com.example.notification.domain.dto;

import com.example.notification.domain.NotificationItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationListResp {
    private List<NotificationItem> items;
    private String nextCursor;
    private long unreadCount;
}