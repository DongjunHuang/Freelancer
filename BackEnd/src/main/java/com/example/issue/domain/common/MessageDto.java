package com.example.issue.domain.common;

import java.time.Instant;

import com.example.issue.domain.user.UserType;
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