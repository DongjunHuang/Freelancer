package com.example.requests;

import lombok.Data;

@Data
public class SignupReq {
    private String username;
    private String password;
    private String email; 
}
