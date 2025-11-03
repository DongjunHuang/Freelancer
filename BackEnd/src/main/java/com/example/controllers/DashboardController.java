package com.example.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.services.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

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
}
