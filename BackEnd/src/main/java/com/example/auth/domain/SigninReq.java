package com.example.auth.domain;

import lombok.Data;

@Data
public class SigninReq {
    private String username;
    private String password;
}