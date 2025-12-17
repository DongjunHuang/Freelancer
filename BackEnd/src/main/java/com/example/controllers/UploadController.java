package com.example.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.exception.ErrorCode;
import com.example.models.DataProps;
import com.example.repos.DatasetMetadata;
import com.example.requests.DatasetMetadataResp;
import com.example.requests.DatasetReq;
import com.example.security.JwtUserDetails;
import com.example.services.MetadataService;
import com.example.services.UploadService;
import com.example.exception.AuthenticationException;
import com.example.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

/**
 * UploadController takes request from user to inject CSV file into the mongodb.
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    // The upload service
    private final UploadService uploadService;

    // The metadata service
    private final MetadataService metadataService;

    /**
     * The main entry to upload data to the MongoDB
     * 
     * @param file the file stream sent by user.
     * @param req  the dataset request data.
     * @param auth the auth information.
     * 
     * @return the result.
     */
    @PostMapping(value = "/uploadCsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestPart("dataset") DatasetReq req,
            Authentication auth) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorCode.NOT_VALID_FILE);
        }
        if (req == null || req.getDatasetName() == null || req.getDatasetName().isEmpty()) {
            throw new BadRequestException(ErrorCode.NOT_VALID_SET_NAME);
        }

        // First create or update table metadata
        JwtUserDetails user = (JwtUserDetails) auth.getPrincipal();
        if (user == null) {
            throw new AuthenticationException(ErrorCode.NOT_VALID_USER);
        }
        Long userId = user.getId();

        String symbolColumn = req.getRecordSymbolColumnName();
        String symbolColumnUpperCase = symbolColumn == null ? null : symbolColumn.toUpperCase();

        String dateColumn = req.getRecordDateColumnName();
        String dateColumnUpperCase = dateColumn == null ? null : dateColumn.toUpperCase();

        // create props for dataset and record
        DataProps props = DataProps.builder()
                .batchId(UUID.randomUUID().toString())
                .userId(userId)
                .datasetName(req.getDatasetName())
                .recordDateColumnFormat(req.getRecordDateColumnFormat())
                .recordDateColumnName(dateColumnUpperCase)
                .recordSymbolColumnName(symbolColumnUpperCase)
                .newDataset(req.isNewDataset())
                .build();

        logger.info("The user id is {}, passed params is {}", userId, req);

        // Create metadata
        // Step 2: insert records to the document
        long rowCount = uploadService.appendRecords(file, props);

        // Step 3: IMPORTANT: COMMIT THE CHANGE,
        // once commit, user is able to see the data appended
        uploadService.promoteStagedToCurrent(req.getDatasetName(), userId, rowCount);

        return ResponseEntity.ok().body(Map.of("Result", "Success"));
    }

    /**
     * Fetch the datasetmetadata information.
     * 
     * @param auth the user login auth.
     * 
     * @return the result.
     */
    @GetMapping("/fetchDatasets")
    public ResponseEntity<List<DatasetMetadataResp>> fetchDatasets(Authentication auth) {
        JwtUserDetails user = (JwtUserDetails) auth.getPrincipal();
        Long userId = user.getId();
        List<DatasetMetadata> metadatas = metadataService.getUserDatasets(userId);
        if (metadatas == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<DatasetMetadataResp> responses = new ArrayList<>();
        for (int i = 0; i < metadatas.size(); i++) {
            if (metadatas.get(i) != null) {
                responses.add(DatasetMetadataResp.fromDatasetMetadata(metadatas.get(i)));
            }
        }

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/dataset/{name}")
    public ResponseEntity<Void> deleteByName(@PathVariable String name, Authentication auth) {
        // TODO: currently we use auth to fetch the user id, but to decouple
        // authentication, we probably want to
        // create currentUserProvider to provide user data/
        /**
         * @Component
         *            public class CurrentUserProvider {
         *            public Long getUserId() {
         *            Authentication auth =
         *            SecurityContextHolder.getContext().getAuthentication();
         *            return ((JwtUserDetails) auth.getPrincipal()).getUserId();
         *            }
         *            }
         */
        JwtUserDetails user = (JwtUserDetails) auth.getPrincipal();
        Long userId = user.getId();
        metadataService.deleteDatasetByNameAndUserId(name, userId);
        return ResponseEntity.noContent().build();
    }
}
