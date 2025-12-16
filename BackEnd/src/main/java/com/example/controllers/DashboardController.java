package com.example.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.models.FetchRecordsProps;
import com.example.repos.DatasetMetadata;
import com.example.requests.DatasetMetadataResp;
import com.example.requests.FetchRecordsReq;
import com.example.requests.FetchRecordsResp;
import com.example.security.JwtUserDetails;
import com.example.services.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    @GetMapping("/fetchDatasets")
    public ResponseEntity<List<DatasetMetadataResp>> fetchDatasets(Authentication auth) {
        JwtUserDetails user = (JwtUserDetails) auth.getPrincipal();
        Long userId = user.getId();
        List<DatasetMetadata> metadatas = dashboardService.getUserDatasets(userId);
        List<DatasetMetadataResp> responses = new ArrayList<>();
        for (int i = 0; i < metadatas.size(); i++) {
            responses.add(DatasetMetadataResp.fromDatasetMetadata(metadatas.get(i)));
        }

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/queryDatapoints")
    public ResponseEntity<FetchRecordsResp> queryDatapoints(
            Authentication auth,
            @RequestBody FetchRecordsReq req) {
        Long userId = ((JwtUserDetails) auth.getPrincipal()).getId();
        logger.info("Handle the fetch datapoints request from user {} with request {}", userId, req);

        FetchRecordsProps props = FetchRecordsProps.fromFetchRecordsReq(req);
        FetchRecordsResp resp = dashboardService.queryDatapoints(userId, props);
        return ResponseEntity.ok(resp);
    }
}
