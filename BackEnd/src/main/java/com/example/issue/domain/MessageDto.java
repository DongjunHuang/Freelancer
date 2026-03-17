package com.example.issue.domain;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private UserType userType;
    private Long senderId;
    private String body;
    private Instant createdAt;
    private boolean isInternal;
}