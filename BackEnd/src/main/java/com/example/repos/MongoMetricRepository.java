package com.example.repos;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoMetricRepository extends MongoRepository<MongoMetric, String> {}