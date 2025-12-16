package com.example.requests;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataPoint {
    LocalDate recordDate; // time stamp
    String symbol; // series
    String column; // metric
    Double value; // Value
}