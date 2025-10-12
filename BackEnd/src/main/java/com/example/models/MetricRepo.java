package com.example.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;

public interface MetricRepo extends JpaRepository<Metric, Long> {
  @Query("select m from Metric m where m.ts between :from and :to order by m.ts asc")
  List<Metric> findRange(Instant from, Instant to);
}