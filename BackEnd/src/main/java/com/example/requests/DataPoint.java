package com.example.requests;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;


@Data
public class DataPoint {   
    private LocalDate recordDate;        
    private Map<String, Object> values;    
}