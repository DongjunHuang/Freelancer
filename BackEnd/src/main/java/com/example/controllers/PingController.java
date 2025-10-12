package com.example.controllers;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class PingController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Freelancer backend!";
    }
}
