package com.example.functiontest.domain;

import lombok.Data;
import java.time.Instant;

@Data
public class CreateMetricReq {
    private Instant ts;
    private Double value;
}