package com.example.auth.domain.user;

import lombok.Data;

@Data
public class SignupReq {
    private String username;
    private String password;
    private String email;
}
