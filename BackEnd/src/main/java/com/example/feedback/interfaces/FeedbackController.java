package com.example.feedback.interfaces;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.feedback.app.FeedbackService;
import com.example.feedback.domain.CreateThreadReq;
import com.example.feedback.domain.CursorPageDto;
import com.example.feedback.domain.PostMessageReq;
import com.example.feedback.domain.ThreadDetailDto;
import com.example.feedback.domain.ThreadItemDto;
import com.example.feedback.domain.ThreadPageResp;
import com.example.feedback.domain.ThreadStatus;
import com.example.security.JwtUserDetails;

/**
 * Feedback api to record the user feedback related api.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService service;

    /**
     * The API to allow user create message thread.
     *
     * @param req  the requests.
     * @param user the user.
     * @return whether creating thread is successful.
     */
    @PostMapping("/createThread")
    public ResponseEntity<?> createThread(
            @RequestBody CreateThreadReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        service.createThread(user.getId(), req);
        return ResponseEntity.ok().body("Successfully create the topic.");
    }

    @PostMapping("/{threadId}/messages")
    public void postMessage(
            @PathVariable Long threadId,
            @RequestBody PostMessageReq req,
            @AuthenticationPrincipal JwtUserDetails user) {
        service.postMessageByUser(user.getId(), threadId, req);
    }

    @GetMapping("/getThreads")
    public ResponseEntity<ThreadPageResp> getThreads(
            @RequestParam(required = false) ThreadStatus status,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal JwtUserDetails user) {
        int pageSize = Math.min(Math.max(size, 1), 50);

        CursorPageDto<ThreadItemDto> cursorPageDto = service.listUserThreads(
                user.getId(),
                status,
                pageSize,
                cursor);
        ThreadPageResp resp = ThreadPageResp.fromCursorPageDto(cursorPageDto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{threadId}")
    public ThreadDetailDto detail(@PathVariable Long threadId) {
        // TODO return service.getUserThreadDetail(currentUserId(), threadId);
        return null;
    }
}