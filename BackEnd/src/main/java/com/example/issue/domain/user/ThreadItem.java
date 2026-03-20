package com.example.issue.domain.user;

import java.time.Instant;

import com.example.issue.domain.common.ThreadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Single thread item.
 */
@Data
@AllArgsConstructor
public class ThreadItem {
    private Long id;
    private String title;
    private ThreadStatus status;
    private Instant lastMessageAt;
    private Instant createdAt;
    private int unreadByUser;
    private int unreadByAdmin;
}