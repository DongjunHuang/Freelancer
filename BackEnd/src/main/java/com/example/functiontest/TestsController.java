package com.example.functiontest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.models.MetricDTO;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
public class TestsController {
    private static final Logger logger = LoggerFactory.getLogger(TestsController.class);

    private final MetricService svc;
    private final TestEmailService testEmailService;

    @PostMapping("/insert")
    public MetricDTO insert(@RequestBody CreateMetricReq req){
        logger.info("Metric value is: " + req.getValue() + " and time is " + req.getTs());
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