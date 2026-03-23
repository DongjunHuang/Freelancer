package com.example.issue.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The container of the page.
 */
@Data
@AllArgsConstructor
public class CursorPageDto<T> {
    private List<T> items;
    private String nextCursor;
    private boolean hasMore;
}