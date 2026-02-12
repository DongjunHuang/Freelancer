package com.example.functiontest.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.functiontest.app.MetricService;
import com.example.functiontest.app.TestEmailService;
import com.example.functiontest.domain.CreateMetricReq;
import com.example.functiontest.domain.MetricDTO;
import com.example.functiontest.domain.MetricRequestNosqlReq;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * This controller used to mainly for testing purpose. Should not visible to
 * users.
 */
@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
public class TestsController {
    private static final Logger logger = LoggerFactory.getLogger(TestsController.class);

    private final MetricService svc;
    private final TestEmailService testEmailService;

    @PostMapping("/insert")
    public MetricDTO insert(@RequestBody CreateMetricReq req) {
        logger.info("Metric value is: " + req.getValue() + " and time is " + req.getTs());
        return svc.insert(req);
    }

    @GetMapping("/list")
    public List<MetricDTO> list(@RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return svc.findRange(Instant.parse(from), Instant.parse(to));
    }

    @GetMapping("/getNumber")
    public long getNumber() {
        return svc.getNumber();
    }

    @PostMapping("/insertNosql")
    public ResponseEntity<Map<String, Object>> insertNosql(@RequestBody MetricRequestNosqlReq req) {
        logger.info("NosqlMetric value is: " + req.getValue());
        return ResponseEntity.ok(svc.insertNosqlMetric(req));
    }

    @GetMapping("/getNumberNosql")
    public long getNumberNosql() {
        return svc.getNumberNosql();
    }

    @GetMapping("/sendEmailTest")
    public void sendEmailTest() {
        logger.info("Received the sending email tests");
        testEmailService.sendVerificationMail("12345");
    }
}