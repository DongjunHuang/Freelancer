package com.example.controllers;

import org.springframework.web.bind.annotation.*;

import com.example.models.MetricDTO;
import com.example.services.MetricService;

import java.time.Instant;
import java.util.List;

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
}