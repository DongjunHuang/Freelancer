package com.example.notification.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUnreadSummaryResp {
    private long unreadCount;
    private List<NotificationCategorySummaryDto> categories;
}