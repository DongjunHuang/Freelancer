package com.example.issue.domain.admin;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminThreadStatsResp {
    private long all;
    private long waitingAdmin;
    private long waitingUser;
    private long resolved;
    private long open;
}