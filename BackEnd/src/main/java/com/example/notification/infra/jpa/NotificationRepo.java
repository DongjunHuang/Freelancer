package com.example.notification.infra.jpa;

import com.example.notification.domain.Notification;
import com.example.notification.domain.NotificationRecipientType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Notification n
        SET n.isRead = true, n.readAt = :readAt
        WHERE n.recipientType = :recipientType
          AND n.recipientId = :recipientId
          AND n.id IN :ids
          AND n.isRead = false
    """)
    int markReadByIds(
            @Param("recipientType") NotificationRecipientType recipientType,
            @Param("recipientId") Long recipientId,
            @Param("ids") List<Long> ids,
            @Param("readAt") Instant readAt
    );

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.recipientType = :recipientType
          AND n.recipientId = :recipientId
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findFirstPage(
            @Param("recipientType") NotificationRecipientType recipientType,
            @Param("recipientId") Long recipientId,
            Pageable pageable
    );

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.recipientType = :recipientType
          AND n.recipientId = :recipientId
          AND (
                n.createdAt < :cursorCreatedAt
                OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId)
          )
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findNextPage(
            @Param("recipientType") NotificationRecipientType recipientType,
            @Param("recipientId") Long recipientId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    Optional<Notification> findFirstByRecipientTypeAndRecipientIdAndGroupKeyAndIsReadFalse(
            NotificationRecipientType recipientType,
            Long recipientId,
            String groupKey
    );

    long countByRecipientTypeAndRecipientIdAndIsReadFalse(
            NotificationRecipientType recipientType,
            Long recipientId
    );

    // ====================================================================================================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Notification n
        SET n.isRead = true,
            n.readAt = :readAt
        WHERE n.recipientType = :recipientType
          AND n.recipientId = :recipientId
          AND n.isRead = false
    """)
    int markAllRead(
            @Param("recipientType") NotificationRecipientType recipientType,
            @Param("recipientId") Long recipientId,
            @Param("readAt") Instant readAt
    );

    @Query("""
        SELECT
            n.category as category,
            COUNT(n) as unreadCount
        FROM Notification n
        WHERE n.recipientType = :recipientType
          AND n.recipientId = :recipientId
          AND n.isRead = false
        GROUP BY n.category
        ORDER BY COUNT(n) DESC
    """)
    List<NotificationCategoryCountProjection> countUnreadByCategory(
            @Param("recipientType") NotificationRecipientType recipientType,
            @Param("recipientId") Long recipientId
    );
}