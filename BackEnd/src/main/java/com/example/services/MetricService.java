package com.example.services;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.models.MetricDTO;
import com.example.repos.Metric;
import com.example.repos.MetricRepository;
import com.example.repos.MongoMetric;
import com.example.repos.MongoMetricRepository;
import com.example.requests.CreateMetricReq;
import com.example.requests.MetricRequestNosql;

import lombok.NonNull;

@Service
public class MetricService {

    private final MetricRepository metricRepository;
    private final MongoTemplate template;
    private final MongoMetricRepository mongoMetricRepository;
    
    public MetricService(MetricRepository metricRepository, MongoTemplate template, MongoMetricRepository mongoMetricRepository) {
        this.metricRepository = metricRepository;
        this.template = template;
        this.mongoMetricRepository = mongoMetricRepository;
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

    public Map<String, Object> insertNosqlMetric(MetricRequestNosql req) {
        MongoMetric saved = mongoMetricRepository.save(new MongoMetric(
                req.getUserId(),
                req.getValue(),
                req.getKind(),
                req.getExtra()
        ));

        long global = incAndGet("metrics_total");
        long perUser = incAndGet("metrics_total_user_" + req.getUserId());
        Long perKind = null;
        if (req.getKind() != null && !req.getKind().isBlank()) {
            perKind = incAndGet("metrics_total_kind_" + req.getKind());
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", saved.getId());
        resp.put("userId", saved.getUserId());
        resp.put("value", saved.getValue());
        resp.put("kind", saved.getKind());
        resp.put("globalTotal", global);
        resp.put("userTotal", perUser);
        if (perKind != null) resp.put("kindTotal", perKind);
        return resp;
    }

    public long getNumberNosql() {
        return template.getCollection("metrics").countDocuments();
    }

    /** 使用 findAndModify + upsert + returnNew 原子自增并返回新值 */
    private long incAndGet(String counterId) {
        Query q = Query.query(Criteria.where("_id").is(counterId));
        Update u = new Update().inc("value", 1);
        FindAndModifyOptions opts = FindAndModifyOptions.options().upsert(true).returnNew(true);
        Document updated = template.findAndModify(q, u, opts, Document.class, "counters");
        return updated.get("value", Number.class).longValue();
    }
}
