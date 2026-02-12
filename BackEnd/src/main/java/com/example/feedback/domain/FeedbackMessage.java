package com.example.feedback.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "feedback_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "thread_id", nullable = false)
    private Long threadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 16)
    private SenderType senderType;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "is_internal", nullable = false)
    private boolean isInternal;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}