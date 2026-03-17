package com.example.auth.domain.admin;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminSigninResp {
    private String accessToken;
    private long expiresIn;
    private AdminProfileDto admin;
}