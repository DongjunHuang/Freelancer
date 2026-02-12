package com.example.feedback.domain;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Single thread item.
 */
@Data
@AllArgsConstructor
public class ThreadItemDto {
    private Long id;
    private String title;
    private ThreadStatus status;
    private Instant lastMessageAt;
    private int unreadByUser;
    private int unreadByAdmin;
}