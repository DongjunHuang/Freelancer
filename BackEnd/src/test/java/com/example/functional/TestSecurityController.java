package com.example.functional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecurityController {

    @GetMapping("/auth/ping")
    public String authPing() {
        return "OK_AUTH";
    }

    @GetMapping("/secure/ping")
    public String securePing() {
        return "OK_SECURE";
    }
}