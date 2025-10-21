package com.example.repos;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {
    @Query("select m from Metric m where m.ts between :from and :to order by m.ts asc")
    public List<Metric> findRange(Instant from, Instant to);
}
