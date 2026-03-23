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

        logger.info("Create message thread for user {}", user.getId());
        service.createThread(user.getId(), req);
        return ResponseEntity.ok().body(Map.of("Result", "Success"));
    }

    @GetMapping("/getThreads")
    public ResponseEntity<ThreadPageResp> getThreads(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails user) {
        logger.info("Get message thread for user {} for status {}", user.getId(), status);

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
    public ResponseEntity<?> postMessage(
            @PathVariable Long threadId,
            @RequestBody PostMessageReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        service.postMessage(user.getId(), threadId, req, UserType.USER);
        return ResponseEntity.ok().body(Map.of("Result", "Success"));
    }

    @GetMapping("/{threadId}/getMessages")
    public ResponseEntity<MessagePageResp> getMessages(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails user) {
        logger.info("Get the message information for user {} and thread id {}", user.getId(), threadId);
        int pageSize = Math.min(Math.max(size, 1), 50);
        // TODO: is internal could be used in the future
        MessagePageResp resp = service.getMessages(user.getId(), threadId, pageSize, cursor, false);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{threadId}")
    public ResponseEntity<ThreadItem> getThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal JwtUserDetails user) {
        logger.info("Get the thread information for user {} and thread id {}", user.getId(), threadId);
        ThreadItem resp = service.getUserThreadDetail(user.getId(), threadId);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{threadId}/status")
    public ResponseEntity<Void> updateThreadStatus(
            @PathVariable Long threadId,
            @RequestBody ThreadStatus status,
            @AuthenticationPrincipal JwtUserDetails user) {
        service.updateUserThreadStatus(user.getId(), threadId, status);
        return ResponseEntity.ok().build();
    }
}