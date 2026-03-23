package com.example.issue.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessagePageResp {
    private List<MessageItem> items;
    private String nextCursor;
    private boolean hasMore;
}