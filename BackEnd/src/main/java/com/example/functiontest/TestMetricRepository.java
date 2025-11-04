package com.example.functiontest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestMetricRepository extends MongoRepository<MongoMetric, String> {}