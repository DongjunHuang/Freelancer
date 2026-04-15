package com.example.notification.interfaces;

import com.example.notification.app.NotificationService;
import com.example.notification.domain.NotificationListResp;
import com.example.notification.domain.NotificationRecipientType;
import com.example.notification.domain.NotificationUnreadSummaryResp;
import com.example.notification.domain.NotificationsMarkReadReq;
import com.example.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/notification")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public NotificationListResp getNotifications(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        return notificationService.getNotifications(
                NotificationRecipientType.ADMIN,
                principal.getId(),
                cursor,
                size
        );
    }

    @GetMapping("/unreadCount")
    public long getUnreadCount(@AuthenticationPrincipal JwtUserDetails principal) {
        return notificationService.getUnreadCount(
                NotificationRecipientType.ADMIN,
                principal.getId()
        );
    }

    @PostMapping("/markRead")
    public void markRead(
            @RequestBody NotificationsMarkReadReq req,
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        notificationService.markRead(
                NotificationRecipientType.ADMIN,
                principal.getId(),
                req.getIds()
        );
    }


    @GetMapping("/unread-summary")
    public NotificationUnreadSummaryResp getNotificationUnreadSummary(
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        return notificationService.getUnreadSummary(
                NotificationRecipientType.ADMIN,
                principal.getId()
        );
    }
}