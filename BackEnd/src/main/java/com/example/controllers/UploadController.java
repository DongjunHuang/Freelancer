package com.example.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.models.DataProps;
import com.example.repos.DatasetMetadata;
import com.example.requests.DatasetReq;
import com.example.security.JwtUserDetails;
import com.example.services.MetadataService;
import com.example.services.UploadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    
    private final UploadService uploadService;
    private final MetadataService dashboardService;

    @PostMapping(value = "/uploadCsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadWithNewDataset(
        @RequestParam("file") MultipartFile file,
        @RequestPart("dataset") DatasetReq req,
        Authentication auth) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "The file is empty"));
        }
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "table name is empty."));
        }

        // First create or update table metadata
        JwtUserDetails user = (JwtUserDetails) auth.getPrincipal();
        Long userId = user.getId();
        
        // create props for dataset and record
        DataProps props = DataProps.builder()
                            .batchId(UUID.randomUUID().toString())
                            .userId(userId)
                            .datasetName(req.getDatasetName())
                            .recordDateColumnFormat(req.getRecordDateColumnFormat())
                            .recordDateColumnName(req.getRecordDateColumnName())
                            .build();

        logger.info("The user id is {}, passed params is {}", userId, req);
        
        // Create metadata
        try {

            // Step 2: insert records to the document
            long rowCount = uploadService.appendRecords(file, props);

            // Step 3: update metadata
            uploadService.promoteStagedToCurrent(req.getDatasetName(), userId, rowCount);

        } catch (Exception ex) {
            // Todo: roll back and return error to users
        }
                
        return ResponseEntity.ok().body(Map.of("Result", "Success"));
    }

    @GetMapping("/fetchDatasets")
    public ResponseEntity<List<DatasetMetadata>> myDatasets(Authentication auth) {
        JwtUserDetails user = (JwtUserDetails) auth.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(dashboardService.getUserDatasets(userId));
    }
}

