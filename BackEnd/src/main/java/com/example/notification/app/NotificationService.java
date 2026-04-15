package com.example.notification.app;

import com.example.notification.domain.*;
import com.example.notification.infra.jpa.NotificationCategoryCountProjection;
import com.example.notification.infra.jpa.NotificationRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final NotificationRepo repo;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public NotificationListResp getNotifications(
            NotificationRecipientType recipientType,
            Long recipientId,
            String cursor,
            Integer size
    ) {
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, 50);
        List<Notification> rows;

        if (cursor == null || cursor.isBlank()) {
            rows = repo.findFirstPage(
                    recipientType,
                    recipientId,
                    PageRequest.of(0, pageSize + 1)
            );
        } else {
            NotificationCursor decoded = NotificationCursor.decode(cursor, objectMapper);
            rows = repo.findNextPage(
                    recipientType,
                    recipientId,
                    decoded.getCreatedAt(),
                    decoded.getId(),
                    PageRequest.of(0, pageSize + 1)
            );
        }

        boolean hasMore = rows.size() > pageSize;
        if (hasMore) {
            rows = rows.subList(0, pageSize);
        }

        String nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            Notification last = rows.get(rows.size() - 1);
            nextCursor = NotificationCursor.encode(
                    new NotificationCursor(last.getCreatedAt(), last.getId()), objectMapper
            );
        }

        // TODO: long unreadCount = repo.countUnread(recipientType, recipientId);

        return NotificationListResp.builder()
                .items(rows.stream().map(NotificationItem::toNotificationDto).toList())
                .nextCursor(nextCursor)
                // .unreadCount(unreadCount)
                .build();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(
            NotificationRecipientType recipientType,
            Long recipientId
    ) {
        return repo.countByRecipientTypeAndRecipientIdAndIsReadFalse(
                recipientType,
                recipientId
        );
    }

    @Transactional
    public void createNotification(NotificationCommand cmd) {
        if (cmd == null) {
            return;
        }

        String groupKey = buildGroupKey(cmd);
        Instant now = Instant.now();

        Notification existing = repo
                .findFirstByRecipientTypeAndRecipientIdAndGroupKeyAndIsReadFalse(
                        cmd.getRecipientType(),
                        cmd.getRecipientId(),
                        groupKey
                )
                .orElse(null);

        // We are going to group the message together if the message is already in the database.
        // To decide whether it is unique according to the groupKey.
        if (existing != null) {
            int newCount = existing.getEventCount() == null ? 1 : existing.getEventCount() + 1;

            existing.setEventCount(newCount);
            existing.setLastEventAt(now);
            existing.setTitle(buildGroupedTitle(cmd, newCount));
            existing.setContent(buildGroupedContent(cmd, newCount));

            if (cmd.getActionUrl() != null && !cmd.getActionUrl().isBlank()) {
                existing.setActionUrl(cmd.getActionUrl());
            }

            if (cmd.getTargetType() != null) {
                existing.setTargetType(cmd.getTargetType());
            }

            if (cmd.getTargetId() != null) {
                existing.setTargetId(cmd.getTargetId());
            }

            return;
        }

        Notification notification = new Notification();
        notification.setRecipientType(cmd.getRecipientType());
        notification.setRecipientId(cmd.getRecipientId());
        notification.setType(cmd.getType());

        // Set title of the notification
        notification.setTitle(buildGroupedTitle(cmd, 1));
        notification.setContent(buildGroupedContent(cmd, 1));

        notification.setTargetType(cmd.getTargetType());
        notification.setTargetId(cmd.getTargetId());
        notification.setActionUrl(cmd.getActionUrl());
        notification.setCategory(cmd.getCategory());

        notification.setGroupKey(groupKey);
        notification.setEventCount(1);

        notification.setRead(false);
        notification.setReadAt(null);

        notification.setFirstEventAt(now);
        notification.setLastEventAt(now);

        repo.save(notification);
    }

    /**
     * Mark all unread notifications as read.
     * @param recipientType the type, if ADMIN or USER.
     * @param recipientId the id.
     * @return
     */
    @Transactional
    public int markAllRead(
            NotificationRecipientType recipientType,
            Long recipientId
    ) {
        if (recipientType == null || recipientId == null) {
            return 0;
        }

        return repo.markAllRead(
                recipientType,
                recipientId,
                Instant.now()
        );
    }

    private String buildGroupKey(NotificationCommand cmd) {
        String recipientType = cmd.getRecipientType() == null ? NotificationRecipientType.NONE.toString() : cmd.getRecipientType().name();
        String type = cmd.getType() == null ? NotificationType.NONE.toString() : cmd.getType().name();
        String targetType = cmd.getTargetType() == null ? NotificationTargetType.NONE.toString() : cmd.getTargetType().name();
        String targetId = cmd.getTargetId() == null ? "0" : String.valueOf(cmd.getTargetId());
        String recipientId = cmd.getRecipientId() == null ? "0" : String.valueOf(cmd.getRecipientId());

        return String.join(":",
                recipientType,
                recipientId,
                type,
                targetType,
                targetId
        );
    }

    private String buildGroupedTitle(NotificationCommand cmd, int count) {
        if (cmd.getType() == NotificationType.ISSUE_NEW_MESSAGE) {
            return count <= 1
                    ? "New reply on your issue"
                    : count + " new replies on your issue";
        }

        if (cmd.getType() == NotificationType.ISSUE_STATUS_CHANGED) {
            return cmd.getTitle() != null && !cmd.getTitle().isBlank()
                    ? cmd.getTitle()
                    : "Issue status updated";
        }

        if (count <= 1) {
            return safeText(cmd.getTitle(), "New notification");
        }

        return count + " new notifications";
    }

    private String buildGroupedContent(NotificationCommand cmd, int count) {
        if (cmd.getType() == NotificationType.ISSUE_NEW_MESSAGE) {
            if (count <= 1) {
                return safeText(cmd.getContent(), "You have a new reply.");
            }
            return count + " new messages in this thread";
        }

        return safeText(cmd.getContent(), "");
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    /**
     * Mark a set of notifications as read, mostly one.
     * @param recipientType the recipient type.
     * @param recipientId the USER or ADMIN id.
     * @param ids the notification ids.
     */
    @Transactional
    public void markRead(
            NotificationRecipientType recipientType,
            Long recipientId,
            List<Long> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Notification> notifications = repo.findAllById(ids);

        Instant now = Instant.now();
        for (Notification n : notifications) {
            if (n.getRecipientType() != recipientType) {
                continue;
            }
            if (!n.getRecipientId().equals(recipientId)) {
                continue;
            }
            if (!n.isRead()) {
                n.setRead(true);
                n.setReadAt(now);
            }
        }
    }

    @Transactional(readOnly = true)
    public NotificationUnreadSummaryResp getUnreadSummary(
            NotificationRecipientType recipientType,
            Long recipientId
    ) {
        long unreadCount = repo.countByRecipientTypeAndRecipientIdAndIsReadFalse(
                recipientType,
                recipientId
        );

        List<NotificationCategorySummaryDto> categories = repo.countUnreadByCategory(
                        recipientType,
                        recipientId
                ).stream()
                .map(this::toCategorySummaryDto)
                .toList();

        return NotificationUnreadSummaryResp.builder()
                .unreadCount(unreadCount)
                .categories(categories)
                .build();
    }

    private NotificationCategorySummaryDto toCategorySummaryDto(
            NotificationCategoryCountProjection row
    ) {
        NotificationCategory category = row.getCategory();

        return NotificationCategorySummaryDto.builder()
                .category(category)
                .label(toCategoryLabel(category))
                .unreadCount(row.getUnreadCount())
                .build();
    }

    /**
     * Map the category to the label, which displays in the UX.
     * @param category the category.
     * @return the String shows in the UX.
     */
    private String toCategoryLabel(NotificationCategory category) {
        return switch (category) {
            case COMMUNICATION -> "Communication";
            case SYSTEM -> "System";
        };
    }
}