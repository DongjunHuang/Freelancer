package com.example.issue.interfaces;

import com.example.issue.app.IssueService;
import com.example.issue.domain.*;
import com.example.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/issues")
public class AdminIssueController {
    private static final Logger logger = LoggerFactory.getLogger(AdminIssueController.class);
    private final IssueService issueService;

    @GetMapping("/getThreads")
    public ResponseEntity<ThreadPageResp> getThreads(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails admin) {
        int pageSize = Math.min(Math.max(size, 1), 50);

        CursorPageDto<ThreadItem> cursorPageDto = issueService.getThreads(
                admin.getId(),
                status,
                pageSize,
                cursor,
                UserType.ADMIN
        );
        issueService.fillUserIdFieldForAdmin(cursorPageDto.getItems());
        ThreadPageResp resp = ThreadPageResp.fromCursorPageDto(cursorPageDto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/thread-stats")
    public ResponseEntity<ThreadStatsResp> getThreadStats(
            @AuthenticationPrincipal JwtUserDetails admin) {
        ThreadStatsResp resp = issueService.getThreadStats();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{threadId}/postMessage")
    public ResponseEntity<PostMessageResp> postMessage(
            @PathVariable Long threadId,
            @RequestBody PostMessageReq req,
            @AuthenticationPrincipal JwtUserDetails admin) {
        PostMessageResp resp = issueService.postMessage(UserType.ADMIN, admin.getId(), threadId, req);
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/{threadId}/getMessages")
    public ResponseEntity<MessagePageResp> getMessages(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails admin) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        MessagePageResp resp = issueService.getMessages(UserType.ADMIN, null, threadId, pageSize, cursor, false);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{threadId}")
    public ResponseEntity<ThreadItem> getThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal JwtUserDetails admin) {
        ThreadItem resp = issueService.getAdminThreadDetail(threadId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{threadId}/messages/latest")
    public ResponseEntity<MessagePageResp> getLatestMessages(
            @PathVariable Long threadId,
            @RequestParam Instant after,
            @AuthenticationPrincipal JwtUserDetails admin) {
        MessagePageResp resp = issueService.getLatestUserMessages(
                UserType.ADMIN,
                null,
                threadId,
                after
        );

        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{threadId}/markAsRead")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long threadId,
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        issueService.markAsRead(UserType.ADMIN, null, threadId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{threadId}/status")
    public ResponseEntity<Void> updateThreadStatus(
            @PathVariable Long threadId,
            @RequestBody UpdateThreadStatusReq req,
            @AuthenticationPrincipal JwtUserDetails admin) {
        issueService.updateThreadStatus(UserType.ADMIN, null, threadId, req.getStatus());
        return ResponseEntity.ok().build();
    }
}