package com.example.notification.interfaces;

import com.example.notification.app.NotificationService;
import com.example.notification.domain.*;
import com.example.notification.domain.dto.*;
import com.example.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public NotificationListResp getNotifications(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        return notificationService.getNotifications(
                NotificationRecipientType.USER,
                principal.getId(),
                cursor,
                size
        );
    }

    @PostMapping("/markRead")
    public void markRead(
            @RequestBody NotificationsMarkReadReq req,
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        notificationService.markRead(
                NotificationRecipientType.USER,
                principal.getId(),
                req.getIds()
        );
    }

    @GetMapping("/unreadCount")
    public NotificationUnreadCountResp getUnreadCount(
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        long unreadCount = notificationService.getUnreadCount(
                NotificationRecipientType.USER,
                principal.getId()
        );
        return new NotificationUnreadCountResp(unreadCount);
    }

    @PostMapping("/markAllRead")
    public NotificationsMarkAllReadResp markNotificationsAllRead(
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        Long userId = principal.getId();

        int updatedCount = notificationService.markAllRead(
                NotificationRecipientType.USER,
                userId
        );

        long unreadCount = notificationService.getUnreadCount(
                NotificationRecipientType.USER,
                userId
        );

        return NotificationsMarkAllReadResp.builder()
                .updatedCount(updatedCount)
                .unreadCount(unreadCount)
                .build();
    }

    @GetMapping("/unreadSummary")
    public NotificationUnreadSummaryResp getNotificationUnreadSummary(
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        return notificationService.getUnreadSummary(
                NotificationRecipientType.USER,
                principal.getId()
        );
    }
}