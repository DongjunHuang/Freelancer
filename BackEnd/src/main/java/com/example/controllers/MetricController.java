package com.example.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.models.MetricDTO;
import com.example.requests.CreateMetricReq;
import com.example.requests.MetricRequestNosql;
import com.example.services.MetricService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricController {
  private final MetricService svc;

  public MetricController(MetricService svc){ 
    this.svc = svc; 
  }

  @PostMapping("/insert")
  public MetricDTO insert(@RequestBody CreateMetricReq req){
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
  public ResponseEntity<Map<String, Object>> insertNosql(@RequestBody MetricRequestNosql req) {
      return ResponseEntity.ok(svc.insertNosqlMetric(req));
  }

  @GetMapping("/getNumberNosql")
  public long getNumberNosql() {
      return svc.getNumberNosql();
  }
}