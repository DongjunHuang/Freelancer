package com.example.issue.domain.user;

import com.example.issue.domain.common.IssueType;
import com.example.issue.domain.common.ThreadStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ThreadDto {
    private Long id;
    private String title;
    private ThreadStatus status;
    private IssueType type;
    private Instant createdAt;
    private Instant lastMessageAt;
    private int unreadByUser;
    private int unreadByAdmin;
}