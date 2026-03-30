package com.example.auth.domain.user;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class SigninResp {

    private String accessToken;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private String username;
        private String email;
    }
}