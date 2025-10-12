package com.example.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetListOfCompanies {
    
    @GetMapping("/")
    public String getListOfUSCompanies() {
        return "Hello";
    }
}
