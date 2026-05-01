package com.example.dataset.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoredObjectInfo {
    private final String bucket;
    private final String objectKey;
    private final String filePath;
    private final String originalFilename;
    private final String storedFilename;
    private final String contentType;
    private final long fileSize;
}
