package com.example.issue.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ThreadMessageDto {
    private Long id;
    private Long threadId;
    private UserType userType;
    private Long senderId;
    private String body;
    private Boolean isInternal;
    private Instant createdAt;
}