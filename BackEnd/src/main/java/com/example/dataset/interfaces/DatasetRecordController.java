package com.example.dataset.interfaces;

import com.example.dataset.app.DatasetRecordQueryService;
import com.example.dataset.domain.dto.QueryRecordsReq;
import com.example.dataset.domain.dto.QueryRecordsResp;
import com.example.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/datasets/{datasetId}/records")
@RequiredArgsConstructor
public class DatasetRecordController {
    private final DatasetRecordQueryService datasetRecordQueryService;

    @PostMapping("/query")
    public ResponseEntity<QueryRecordsResp> query(
            @PathVariable String datasetId,
            @RequestBody QueryRecordsReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        Long userId = user.getId();
        QueryRecordsResp resp = datasetRecordQueryService.queryRecords(userId, datasetId, req);
        return ResponseEntity.ok(resp);
    }
}
