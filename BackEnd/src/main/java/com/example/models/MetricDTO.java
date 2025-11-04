package com.example.models;

import java.time.Instant;

import com.example.functiontest.Metric;


public record MetricDTO(Instant ts, Double value) {
  public static MetricDTO from(Metric m) {
    return new MetricDTO(m.getTs(), m.getValue());
  }
}