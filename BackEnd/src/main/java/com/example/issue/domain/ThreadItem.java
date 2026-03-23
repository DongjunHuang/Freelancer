package com.example.issue.domain;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Single thread item.
 */
@Data
@AllArgsConstructor
@Builder
public class ThreadItem {
    private Long id;
    private Long userId;
    private String title;
    private ThreadStatus status;
    private Instant lastMessageAt;
    private Instant createdAt;
    private int unreadByUser;
    private int unreadByAdmin;

    public static ThreadItem from(IssueThread t) {
        return ThreadItem.builder()
                .id(t.getId())
                .title(t.getTitle())
                .status(t.getStatus())
                .lastMessageAt(t.getLastMessageAt())
                .createdAt(t.getCreatedAt())
                .unreadByUser(t.getUnreadByUser())
                .unreadByAdmin(t.getUnreadByAdmin())
                .build();
    }
}