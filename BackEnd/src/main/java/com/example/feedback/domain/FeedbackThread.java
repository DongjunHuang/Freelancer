package com.example.feedback.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "feedback_thread")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackThread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ThreadStatus status;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "unread_by_user", nullable = false)
    private int unreadByUser;

    @Column(name = "unread_by_admin", nullable = false)
    private int unreadByAdmin;

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (lastMessageAt == null) {
            lastMessageAt = now;
        }
        if (status == null) {
            status = ThreadStatus.WAITING_ADMIN;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}