package com.example.issue.interfaces;

import com.example.issue.domain.common.MessagePageResp;
import com.example.issue.domain.common.PostMessageReq;
import com.example.issue.domain.user.*;
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

        CursorPageDto<ThreadItem> cursorPageDto = service.listUserThreads(
                user.getId(),
                status,
                pageSize,
                cursor);
        ThreadPageResp resp = ThreadPageResp.fromCursorPageDto(cursorPageDto);
        logger.info("Respond with {}", resp);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{threadId}/messages")
    public ResponseEntity<?> postMessage(
            @PathVariable Long threadId,
            @RequestBody PostMessageReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        service.postMessageByUser(user.getId(), threadId, req);
        return ResponseEntity.ok().body(Map.of("Result", "Success"));
    }


    @GetMapping("/{threadId}/messages")
    public ResponseEntity<MessagePageResp> getThreadMessages(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtUserDetails user) {
        logger.info("Get the message information for user {} and thread id {}", user.getId(), threadId);
        int pageSize = Math.min(Math.max(size, 1), 50);
        MessagePageResp resp = service.getUserThreadMessages(user.getId(), threadId, pageSize);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{threadId}")
    public ResponseEntity<ThreadDto> getThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal JwtUserDetails user) {
        logger.info("Get the thread information for user {} and thread id {}", user.getId(), threadId);
        ThreadDto resp = service.getUserThreadDetail(user.getId(), threadId);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{threadId}/status")
    public ResponseEntity<Void> updateThreadStatus(
            @PathVariable Long threadId,
            @RequestBody UpdateThreadStatusReq req,
            @AuthenticationPrincipal JwtUserDetails user) {

        service.updateUserThreadStatus(user.getId(), threadId, req);
        return ResponseEntity.ok().build();
    }
}