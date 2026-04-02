package com.example.issue.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostMessageResp {
    private MessageItem message;
    private ThreadItem thread;
}
