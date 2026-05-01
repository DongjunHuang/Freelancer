package com.example.functiontest.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MetricDTO {
    private Instant ts;
    private Double value;

    public static MetricDTO from(Metric metric) {
        return MetricDTO.builder().ts(metric.getTs()).value(metric.getValue()).build();
    }
}