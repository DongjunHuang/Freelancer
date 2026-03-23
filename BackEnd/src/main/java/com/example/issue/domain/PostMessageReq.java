package com.example.issue.domain;

import lombok.Data;

@Data
public class PostMessageReq {
    private String body;
    private boolean isInternal;
}