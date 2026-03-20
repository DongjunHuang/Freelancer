package com.example.issue.domain.admin;

import com.example.issue.domain.common.IssueType;
import com.example.issue.domain.common.ThreadStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AdminThreadItemDto {
    private Long id;
    private Long userId;
    private String title;
    private ThreadStatus status;
    private IssueType type;
    private Instant createdAt;
    private Instant lastMessageAt;
    private int unreadByAdmin;
    private int unreadByUser;
}