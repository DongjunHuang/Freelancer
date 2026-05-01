package com.example.notification.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationUnreadCountResp {
    private long unreadCount;
}