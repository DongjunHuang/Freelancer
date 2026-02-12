package com.example.feedback.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ThreadPageResp {
    List<ThreadItemDto> items;
    private String nextCursor;
    private boolean hasMore;

    public static ThreadPageResp fromCursorPageDto(CursorPageDto<ThreadItemDto> cursorPageDto) {
        return ThreadPageResp.builder()
                .items(cursorPageDto.getItems())
                .nextCursor(cursorPageDto.getNextCursor())
                .hasMore(cursorPageDto.isHasMore())
                .build();
    }
}
