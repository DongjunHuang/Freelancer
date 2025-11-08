package com.example.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.functiontest.MetricService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DataRecordController {
    private final MetricService svc;

    @PostMapping("/displaylist")
    public void showlost(){
        // TODO
    }

    @PostMapping("/loadData")
    public void loaddata(){
        // TODO
    }

    @GetMapping("/getNumberSql")
    public long onRefreshAccessTokenRequest() {
        return svc.getNumber();
    }
}