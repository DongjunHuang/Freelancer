package com.example.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.functiontest.MetricService;
import com.example.services.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    private final MetricService svc;

    @PostMapping("/uploadCsv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
            if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("The file is empty");
        }

        try {
            dashboardService.processCsv(file);
            return ResponseEntity.ok("File Handled successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("File to process CSV file: " + e.getMessage());
        }
    }

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