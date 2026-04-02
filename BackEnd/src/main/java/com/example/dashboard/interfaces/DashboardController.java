package com.example.dashboard.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.common.dataset.domain.DatasetMetadata;
import com.example.dashboard.app.DashboardService;
import com.example.dashboard.domain.DatasetMetadataResp;
import com.example.dashboard.domain.FetchRecordsProps;
import com.example.dashboard.domain.FetchRecordsReq;
import com.example.dashboard.domain.FetchRecordsResp;
import com.example.security.JwtUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    @GetMapping("/fetchDatasets")
    public ResponseEntity<List<DatasetMetadataResp>> fetchDatasets(@AuthenticationPrincipal JwtUserDetails user) {
        Long userId = user.getId();
        List<DatasetMetadata> metadatas = dashboardService.getUserDatasets(userId);
        List<DatasetMetadataResp> responses = new ArrayList<>();
        for (DatasetMetadata metadata : metadatas) {
            responses.add(DatasetMetadataResp.fromDatasetMetadata(metadata));
        }

        return ResponseEntity.ok(responses);
    }
    @PostMapping("/datasets/{datasetName}/datapoints/query")
    public ResponseEntity<FetchRecordsResp> queryDatapoints(
            @PathVariable String datasetName,
            @RequestBody FetchRecordsReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        Long userId = user.getId();
        FetchRecordsProps props = FetchRecordsProps.fromFetchRecordsReq(datasetName, req);
        FetchRecordsResp resp = dashboardService.queryDatapoints(userId, props);
        return ResponseEntity.ok(resp);
    }
}
