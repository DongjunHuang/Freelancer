package com.example.auth.domain.user;

import lombok.Data;

@Data
public class SigninReq {
    private String username;
    private String password;
}