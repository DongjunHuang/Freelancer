package com.example.feedback.domain;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThreadDetailDto {
    private Long id;
    private Long userId;
    private String title;
    private ThreadStatus status;
    private Instant lastMessageAt;
    private List<MessageDto> messages;
}