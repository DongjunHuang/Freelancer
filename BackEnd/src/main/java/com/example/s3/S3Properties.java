package com.example.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aws.s3")
@Data
@Component
public class S3Properties {
    private String bucket;
    private String basePrefix = "csv-import";
}
