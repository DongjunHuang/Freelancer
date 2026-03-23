package com.example.issue.domain;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadStatsResp {
    private long all;
    private long waitingAdmin;
    private long waitingUser;
    private long resolved;
    private long open;
}