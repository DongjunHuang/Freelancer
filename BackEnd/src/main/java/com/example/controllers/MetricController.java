package com.example.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.models.MetricDTO;
import com.example.requests.CreateMetricReq;
import com.example.requests.MetricRequestNosqlReq;
import com.example.services.MetricService;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricController {
    private static final Logger logger = LoggerFactory.getLogger(MetricController.class);

    private final MetricService svc;

    @PostMapping("/insert")
    public MetricDTO insert(@RequestBody CreateMetricReq req){
        logger.info("Metric value is: " + req.getValue() + " and time is " + req.getTs());
        return svc.insert(req);
    }

    @GetMapping("/list")
    public List<MetricDTO> list(@RequestParam(required=false) String from,
                                @RequestParam(required=false) String to) {
        return svc.findRange(Instant.parse(from), Instant.parse(to));
    }

    @GetMapping("/getNumber")
    public long getNumber() {
        return svc.getNumber();
    }

    @PostMapping("/insertNosql")
    public ResponseEntity<Map<String, Object>> insertNosql(@RequestBody MetricRequestNosqlReq req) {
        logger.info("NosqlMetric value is: " + req.getValue());
        return ResponseEntity.ok(svc.insertNosqlMetric(req));
    }

    @GetMapping("/getNumberNosql")
    public long getNumberNosql() {
        return svc.getNumberNosql();
    }
}