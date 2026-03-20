package com.example.issue.domain.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminThreadPageResp {
    private List<AdminThreadItemDto> items;
    private String nextCursor;
    private boolean hasMore;
}