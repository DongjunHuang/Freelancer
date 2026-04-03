package com.example.issue.interfaces;

import com.example.issue.domain.*;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.issue.app.IssueService;
import com.example.security.JwtUserDetails;

import java.time.Instant;
import java.util.Map;

/**
 * Feedback api to record the user feedback related api.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/issues")
public class IssueController {
    private static final Logger logger = LoggerFactory.getLogger(IssueController.class);
    private final IssueService service;

    /**
     * The API to allow user create message thread.
     *
     * @param req  the requests.
     * @param user the user.
     * @return whether creating thread is successful.
     */
    @PostMapping("/createThread")
    public ResponseEntity<?> createThread(
            @RequestBody CreateIssueThreadReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        service.createThread(user.getId(), req);
        return ResponseEntity.ok().body(Map.of("Result", "Success"));
    }

    @GetMapping("/getThreads")
    public ResponseEntity<ThreadPageResp> getThreads(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails user) {
        // 20 is the default.
        int pageSize = Math.min(Math.max(size, 1), 20);

        CursorPageDto<ThreadItem> cursorPageDto = service.getThreads(
                user.getId(),
                status,
                pageSize,
                cursor,
                UserType.USER);
        ThreadPageResp resp = ThreadPageResp.fromCursorPageDto(cursorPageDto);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{threadId}/postMessage")
    public ResponseEntity<PostMessageResp> postMessage(
            @PathVariable Long threadId,
            @RequestBody PostMessageReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        PostMessageResp resp = service.postMessage(UserType.USER, user.getId(), threadId, req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{threadId}/getMessages")
    public ResponseEntity<MessagePageResp> getMessages(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails user) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        // TODO: is internal could be used in the future
        MessagePageResp resp = service.getMessages(UserType.USER, user.getId(), threadId, pageSize, cursor, false);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{threadId}")
    public ResponseEntity<ThreadItem> getThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal JwtUserDetails user) {
        ThreadItem resp = service.getUserThreadDetail(user.getId(), threadId);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{threadId}/status")
    public ResponseEntity<Void> updateThreadStatus(
            @PathVariable Long threadId,
            @RequestBody UpdateThreadStatusReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        service.updateThreadStatus(UserType.USER, user.getId(), threadId, req.getStatus());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{threadId}/messages/latest")
    public ResponseEntity<MessagePageResp> getLatestMessages(
            @PathVariable Long threadId,
            @RequestParam Instant after,
            @AuthenticationPrincipal JwtUserDetails user) {
        MessagePageResp resp = service.getLatestUserMessages(
                UserType.USER,
                user.getId(),
                threadId,
                after
        );

        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/issues/{threadId}/read")
    public ResponseEntity<Void> markUserRead(
            @PathVariable Long threadId,
            @AuthenticationPrincipal JwtUserDetails principal
    ) {
        service.markAsRead(UserType.USER, principal.getId(), threadId);
        return ResponseEntity.noContent().build();
    }
}