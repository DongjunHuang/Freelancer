package com.example.controllers;

import lombok.Data;
import java.time.Instant;

@Data
public class CreateMetricReq {
    private Instant ts;
    private Double value;
}