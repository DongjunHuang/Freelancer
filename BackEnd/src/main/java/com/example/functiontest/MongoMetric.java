package com.example.functiontest;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Document(collection = "metrics")
@Data
public class MongoMetric {
    @Id
    private String id;

    @Indexed
    private Long userId;
    
    private Integer value;
    private String kind;
    private Map<String, Object> extra;
    private Instant createdAt = Instant.now();

    public MongoMetric(Long userId, Integer value, String kind, Map<String, Object> extra) {
        this.userId = userId;
        this.value = value;
        this.kind = kind;
        this.extra = extra;
    }
}