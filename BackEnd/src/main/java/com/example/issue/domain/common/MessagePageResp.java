package com.example.issue.domain.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessagePageResp {
    private List<ThreadMessageDto> items;
    private String nextCursor;
    private boolean hasMore;
}