package com.example.functiontest.infra.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.functiontest.domain.MongoMetric;

@Repository
public interface TestMetricRepository extends MongoRepository<MongoMetric, String> {
}