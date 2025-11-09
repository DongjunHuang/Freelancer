package com.example.security;

import lombok.Data;

@Data
public class TokenInfo {
    private String email;
    private String username;    
    private String issuedAt;
    private String expiredAt;
}
