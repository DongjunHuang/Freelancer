package com.example.auth.domain.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshResp{
    private String accessToken;
}