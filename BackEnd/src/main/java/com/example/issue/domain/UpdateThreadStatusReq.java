package com.example.issue.domain;

import lombok.Data;

@Data
public class UpdateThreadStatusReq {
    private ThreadStatus status;
}