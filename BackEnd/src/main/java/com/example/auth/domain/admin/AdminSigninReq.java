package com.example.auth.domain.admin;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class AdminSigninReq {
    private String username;
    private String password;
}