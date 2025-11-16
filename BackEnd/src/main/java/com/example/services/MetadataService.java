package com.example.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.repos.DatasetMetadata;
import com.example.repos.DatasetMetadataRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataService {
    private final DatasetMetadataRepo metadataRepo;

    public List<DatasetMetadata> getUserDatasets(Long userId) {
        return metadataRepo.findByUserId(userId);
    }
}
