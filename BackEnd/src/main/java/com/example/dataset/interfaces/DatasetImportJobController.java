package com.example.dataset.interfaces;

import com.example.dataset.app.DatasetImportJobService;
import com.example.dataset.domain.*;
import com.example.dataset.domain.dto.CreateDatasetReq;
import com.example.dataset.domain.dto.CreateDatasetResp;
import com.example.exception.ErrorCode;
import com.example.exception.types.BadRequestException;
import com.example.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/datasets/import")
@RequiredArgsConstructor
public class DatasetImportJobController {
    private final DatasetImportJobService datasetImportJobService;

    @PostMapping(value = "/createDataset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateDatasetResp> createDataset(
            @RequestParam("file") MultipartFile file,
            @RequestPart("dataset") CreateDatasetReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorCode.NOT_VALID_FILE);
        }
        if (req == null || req.getDatasetName() == null || req.getDatasetName().isBlank()) {
            throw new BadRequestException(ErrorCode.NOT_VALID_SET_NAME);
        }

        CreateDatasetResp resp = datasetImportJobService.createImportJob(file, req, user.getId());
        return ResponseEntity.ok(resp);
    }


    @PostMapping(value = "/appendDataset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> appendDataset(
            @RequestParam("file") MultipartFile file,
            @RequestPart("dataset") CreateDatasetReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        return null;
    }
}
