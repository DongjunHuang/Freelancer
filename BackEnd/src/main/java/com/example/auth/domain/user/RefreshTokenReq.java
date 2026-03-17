package com.example.auth.domain.user;

import lombok.Data;

@Data
public class RefreshTokenReq {
    private String refreshToken;
}
