package com.example.controllers;

import org.springframework.web.bind.annotation.*;

import com.example.models.MetricDTO;
import com.example.models.MetricRepo;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricController {
  private final MetricRepo repo;
  public MetricController(MetricRepo repo){ this.repo = repo; }

  @GetMapping
  public List<MetricDTO> list(
      @RequestParam Instant from,
      @RequestParam Instant to) {
    return repo.
          findRange(from, to)
          .stream()
          .map(MetricDTO::from)
          .toList();
  }
}