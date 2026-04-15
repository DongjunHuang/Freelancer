package com.example.notification.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsMarkAllReadResp {
    private int updatedCount;
    private long unreadCount;
}