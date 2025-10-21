package com.example.services;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.controllers.CreateMetricReq;
import com.example.models.MetricDTO;
import com.example.repos.Metric;
import com.example.repos.MetricRepository;

import lombok.NonNull;

@Service
public class MetricService {

    private final MetricRepository metricRepository;

    public MetricService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }
    
    @Transactional
    public MetricDTO insert(@NonNull CreateMetricReq req) {
        Metric m = new Metric();
        
        m.setTs(Objects.requireNonNullElseGet(req.getTs(), Instant::now));
        m.setValue(req.getValue());

        Metric saved = metricRepository.save(m);
        return MetricDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MetricDTO> findRange(Instant from, Instant to) {
        return metricRepository.findRange(from, to)
                .stream()
                .map(MetricDTO::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public long getNumber() {
        return metricRepository.count();
    }
}
