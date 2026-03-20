package com.example.issue.interfaces;

import com.example.issue.app.AdminIssueService;
import com.example.issue.domain.admin.AdminThreadPageResp;
import com.example.issue.domain.admin.AdminThreadStatsResp;
import com.example.issue.domain.common.MessagePageResp;
import com.example.issue.domain.common.PostMessageReq;
import com.example.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/issues")
public class AdminIssueController {
    private static final Logger logger = LoggerFactory.getLogger(AdminIssueController.class);

    private final AdminIssueService issueAdminService;

    @GetMapping("/getThreads")
    public ResponseEntity<AdminThreadPageResp> getThreads(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails admin) {
        logger.info("Get message thread for user {} for status {}", admin.getId(), status);
        int pageSize = Math.min(Math.max(size, 1), 50);

        AdminThreadPageResp resp = issueAdminService.getAdminThreads(
                status,
                pageSize,
                cursor
        );

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/thread-stats")
    public ResponseEntity<AdminThreadStatsResp> getThreadStats(
            @AuthenticationPrincipal JwtUserDetails admin) {
        AdminThreadStatsResp resp = issueAdminService.getThreadStats(admin.getId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{threadId}/messages")
    public ResponseEntity<?> postMessage(
            @PathVariable Long threadId,
            @RequestBody PostMessageReq req,
            @AuthenticationPrincipal JwtUserDetails admin) {
        logger.info("Post by admin for message thread id {}", threadId);
        issueAdminService.postMessageByAdmin(admin.getId(), threadId, req);
        return ResponseEntity.ok().body(Map.of("Result", "Success"));
    }


    @GetMapping("/{threadId}/messages")
    public ResponseEntity<MessagePageResp> getThreadMessages(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtUserDetails admin) {
        logger.info("Get the message information for user {} and thread id {}", admin.getId(), threadId);
        int pageSize = Math.min(Math.max(size, 1), 50);
        MessagePageResp resp = issueAdminService.getAdminThreadMessages(admin.getId(), threadId, pageSize);
        return ResponseEntity.ok(resp);
    }
}