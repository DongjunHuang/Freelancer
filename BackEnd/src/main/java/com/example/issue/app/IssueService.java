package com.example.issue.app;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.issue.domain.common.*;
import com.example.issue.domain.user.*;
import com.example.issue.infra.jpa.IssueMessageRepo;
import com.example.issue.infra.jpa.IssueThreadRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IssueService {
    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);

    private final IssueThreadRepo threadRepo;
    private final IssueMessageRepo messageRepo;
    private final ObjectMapper objectMapper;

    /**
     * Create a new feedback thread and append the first mesasge to the thread.
     *
     * @param userId the user id.
     * @param req    the request.
     */
    @Transactional
    public void createThread(Long userId, CreateIssueThreadReq req) {
        var now = Instant.now();

        IssueThread thread = IssueThread.builder()
                .userId(userId)
                .title(req.getTitle())
                .status(ThreadStatus.WAITING_ADMIN)
                .lastMessageAt(now)
                .unreadByAdmin(1)
                .unreadByUser(0)
                .issueType(req.getType())
                .description(req.getDescription())
                .impact(req.getImpact())
                .build();
        thread = threadRepo.save(thread);

        messageRepo.save(IssueMessage.builder()
                .threadId(thread.getId())
                .userType(UserType.USER)
                .senderId(userId)
                .body(req.getDescription())
                .createdAt(now)
                .isInternal(false)
                .build());
    }

    /**
     * Post mesasge to the corresponding thread from user side.
     *
     * @param userId   the user id.
     * @param threadId the thread id.
     * @param req      the request.
     */
    @Transactional
    public void postMessageByUser(Long userId, Long threadId, PostMessageReq req) {
        var thread = threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FEEDBACK_THREAD_NOT_FOUND));

        var now = Instant.now();
        messageRepo.save(IssueMessage.builder()
                .threadId(threadId)
                .userType(UserType.USER)
                .senderId(userId)
                .body(req.getBody())
                .createdAt(now)
                .isInternal(false)
                .build());

        thread.setStatus(ThreadStatus.WAITING_ADMIN);
        thread.setLastMessageAt(now);
        thread.setUnreadByAdmin(thread.getUnreadByAdmin() + 1);
        threadRepo.save(thread);
    }

    @Transactional(readOnly = true)
    public CursorPageDto<ThreadItem> listUserThreads(
            Long userId,
            String status,
            int size,
            String cursor) {

        List<String> statuses = mapStatuses(status);

        List<IssueThread> rows = null;
        if (cursor == null || cursor.isBlank()) {
            if (statuses == null) {
                rows = threadRepo.listUserFirstPage(userId, size);
            } else {
                logger.info("Fetch for specific statuses {}", statuses);
                rows = threadRepo.listUserFirstPageByStatuses(userId, statuses, size);
            }
        } else {
            var c = ThreadCursor.decode(objectMapper, cursor);
            rows = threadRepo.listUserNextPage(userId, statuses, c.getLastMessageAt(), c.getId(), size);
        }
        logger.info("Received the information from database with size {}", rows.size());

        var items = rows.stream()
                .map(t -> new ThreadItem(
                        t.getId(),
                        t.getTitle(),
                        t.getStatus(),
                        t.getLastMessageAt(),
                        t.getCreatedAt(),
                        t.getUnreadByUser(),
                        t.getUnreadByAdmin()))
                .toList();

        String nextCursor = null;
        boolean hasMore = rows.size() == size;

        if (hasMore) {
            var last = rows.get(rows.size() - 1);
            nextCursor = ThreadCursor.encode(objectMapper,
                    new ThreadCursor(last.getLastMessageAt(), last.getId()));
        }

        return new CursorPageDto<>(items, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public ThreadDto getUserThreadDetail(Long userId, Long threadId) {
        IssueThread thread = threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        return ThreadDto.builder()
                .id(thread.getId())
                .title(thread.getTitle())
                .status(thread.getStatus())
                .lastMessageAt(thread.getLastMessageAt())
                .unreadByUser(thread.getUnreadByUser())
                .unreadByAdmin(thread.getUnreadByAdmin())
                .createdAt(thread.getCreatedAt())
                .build();
    }

    private List<String> mapStatuses(String status) {
        if (status == null || status.isBlank() || "ALL".equals(status)) {
            return null;
        }

        status = status.toUpperCase();

        return switch (status) {
            case "OPEN" -> List.of(ThreadStatus.WAITING_USER.name(), ThreadStatus.WAITING_ADMIN.name());
            case "WAITING_USER" -> List.of(ThreadStatus.WAITING_USER.name());
            case "WAITING_ADMIN" -> List.of(ThreadStatus.WAITING_ADMIN.name());
            case "RESOLVED" -> List.of(ThreadStatus.RESOLVED.name());
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }

    @Transactional(readOnly = true)
    public MessagePageResp getUserThreadMessages(Long userId, Long threadId, int size) {
        threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        List<IssueMessage> rows = messageRepo.listFirstPageByThreadId(threadId, size + 1);

        boolean hasMore = rows.size() > size;
        if (hasMore) {
            rows = rows.subList(0, size);
        }

        List<ThreadMessageDto> items = rows.stream()
                .map(ThreadMessageDto::toThreadMessageDto)
                .toList();

        String nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            IssueMessage last = rows.get(rows.size() - 1);
            nextCursor = Utils.encodeMessageCursor(last.getCreatedAt(), last.getId());
        }

        return MessagePageResp.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    @Transactional
    public void updateUserThreadStatus(Long userId, Long threadId, UpdateThreadStatusReq req) {
        IssueThread thread = threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        ThreadStatus newStatus = req.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("status cannot be null");
        }

        if (newStatus != ThreadStatus.RESOLVED && newStatus != ThreadStatus.WAITING_ADMIN) {
            throw new IllegalArgumentException("unsupported status");
        }

        thread.setStatus(newStatus);
        threadRepo.save(thread);
    }
}