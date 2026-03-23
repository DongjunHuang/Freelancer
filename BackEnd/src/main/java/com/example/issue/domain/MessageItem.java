package com.example.issue.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MessageItem {
    private Long id;
    private Long threadId;
    private UserType userType;
    private Long senderId;
    private String body;
    private Boolean isInternal;
    private Instant createdAt;

    public static MessageItem toMessageItem(IssueMessage message) {
        return MessageItem.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .userType(message.getUserType())
                .senderId(message.getSenderId())
                .body(message.getBody())
                .isInternal(message.isInternal())
                .createdAt(message.getCreatedAt())
                .build();
    }
}