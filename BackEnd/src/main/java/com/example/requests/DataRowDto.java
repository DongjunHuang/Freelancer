package com.example.requests;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;

@Data
public class DataRowDto {
    private LocalDate uploadDate;       
    private LocalDate recordDate;       
    private Map<String, Object> values;    
}