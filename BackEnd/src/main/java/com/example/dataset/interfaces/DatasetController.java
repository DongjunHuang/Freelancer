package com.example.dataset.interfaces;

import com.example.dataset.app.DatasetService;
import com.example.dataset.domain.DatasetMetadata;
import com.example.dataset.domain.dto.Dataset;
import com.example.dataset.domain.dto.DatasetMetadataResp;
import com.example.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/datasets")
@RequiredArgsConstructor
public class DatasetController {
    private final DatasetService datasetService;

    @GetMapping("/getUserDatasets")
    public ResponseEntity<DatasetMetadataResp> getUserDatasets(@AuthenticationPrincipal JwtUserDetails user) {
        Long userId = user.getId();
        List<DatasetMetadata> metadata = datasetService.getUserDatasets(userId);
        List<Dataset> datasets = new ArrayList<>();
        for (DatasetMetadata mt : metadata) {
            datasets.add(Dataset.fromDatasetMetadata(mt));
        }
        DatasetMetadataResp resp = DatasetMetadataResp.builder().datasets(datasets).build();
        return ResponseEntity.ok(resp);
    }

    /**
     * Delete datasets.
     *
     * @param name the name of the datasets.
     * @param user the user.
     * @return the result.
     */
    @DeleteMapping("/{datasetId}")
    public ResponseEntity<Void> deleteDatasetById(
            @PathVariable String name,
            @AuthenticationPrincipal JwtUserDetails user) {
        Long userId = user.getId();
        datasetService.deleteDatasetByNameAndUserId(name, userId);
        return ResponseEntity.noContent().build();
    }
}
