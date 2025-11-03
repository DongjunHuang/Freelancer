// src/main/java/com/example/metrics/dto/MetricRequest.java
package com.example.requests;

import java.util.Map;
import lombok.Data;

@Data
public class MetricRequestNosqlReq {
    private Long userId;                
    private Integer value;               
    private String kind;                 
    private Map<String, Object> extra;   
}