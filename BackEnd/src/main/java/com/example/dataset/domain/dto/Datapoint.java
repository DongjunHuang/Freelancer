package com.example.dataset.domain.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Datapoint {
    LocalDate recordedTime; // time stamp
    String symbol; // series
    String column; // metric
    Double value; // Value
}