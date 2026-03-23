package com.example.issue.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ThreadPageResp {
    List<ThreadItem> items;
    private String nextCursor;
    private boolean hasMore;

    public static ThreadPageResp fromCursorPageDto(CursorPageDto<ThreadItem> cursorPageDto) {
        return ThreadPageResp.builder()
                .items(cursorPageDto.getItems())
                .nextCursor(cursorPageDto.getNextCursor())
                .hasMore(cursorPageDto.isHasMore())
                .build();
    }
}
