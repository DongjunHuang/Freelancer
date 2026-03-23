package com.example.issue.app;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.issue.domain.*;
import com.example.issue.infra.jpa.IssueMessageRepo;
import com.example.issue.infra.jpa.IssueThreadRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
    public void postMessage(Long userId, Long threadId, PostMessageReq req, UserType type) {
        var thread = threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FEEDBACK_THREAD_NOT_FOUND));

        var now = Instant.now();
        messageRepo.save(IssueMessage.builder()
                .threadId(threadId)
                .userType(type)
                .senderId(userId)
                .body(req.getBody())
                .createdAt(now)
                .isInternal(req.isInternal())
                .build());

        thread.setLastMessageAt(now);
        threadRepo.save(thread);
    }

    @Transactional(readOnly = true)
    public CursorPageDto<ThreadItem> getThreads(
            Long userId,
            String status,
            int size,
            String cursor,
            UserType userType) {
        List<String> statuses = mapStatuses(status);
        logger.info("Get threads for user id {}, status {}, size {}, cursor {}", userId, statuses, size, cursor);

        List<IssueThread> rows = listThreads(userId, size, userType, cursor, statuses);

        if (rows == null) {
            throw new NotFoundException(ErrorCode.NOT_FOUND);
        }

        logger.info("Received the information from database with size {}", rows.size());

        var items = rows.stream()
                .map(ThreadItem::from)
                .toList();

        String nextCursor = null;
        boolean hasMore = rows.size() == size;

        if (hasMore) {
            var last = rows.get(rows.size() - 1);
            nextCursor = Cursor.encode(objectMapper,
                    new Cursor(last.getLastMessageAt(), last.getId()));
        }

        return new CursorPageDto<>(items, nextCursor, hasMore);
    }

    public ThreadItem getUserThreadDetail(Long userId, Long threadId) {
        IssueThread thread = threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        return ThreadItem.from(thread);
    }


    public ThreadItem getAdminThreadDetail(Long threadId) {
        IssueThread thread = threadRepo.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        return ThreadItem.from(thread);
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
    public ThreadStatsResp getThreadStats() {
        long all = threadRepo.countAllThreads();
        long waitingAdmin = threadRepo.countByStatus(ThreadStatus.WAITING_ADMIN.name());
        long waitingUser = threadRepo.countByStatus(ThreadStatus.WAITING_USER.name());
        long resolved = threadRepo.countByStatus(ThreadStatus.RESOLVED.name());

        return ThreadStatsResp.builder()
                .all(all)
                .waitingAdmin(waitingAdmin)
                .waitingUser(waitingUser)
                .resolved(resolved)
                .open(waitingAdmin + waitingUser)
                .build();
    }

    @Transactional
    public void updateMessageStatus(Long threadId, ThreadStatus status) {
        var thread = threadRepo.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));
        thread.setStatus(status);
        threadRepo.save(thread);
    }

    @Transactional(readOnly = true)
    public MessagePageResp getMessages(Long userId, Long threadId, int size, String cursor, boolean isInternal) {
        int pageSize = Math.min(Math.max(size, 1), 50);

        threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        List<IssueMessage> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = messageRepo.listFirstPage(threadId, isInternal,pageSize + 1);
        } else {
            Cursor messageCursor = Cursor.decode(objectMapper, cursor);
            rows = messageRepo.listNextPage(
                    threadId,
                    isInternal,
                    messageCursor.getLastMessageAt(),
                    messageCursor.getId(),
                    pageSize + 1
            );
        }

        boolean hasMore = rows.size() > pageSize;
        if (hasMore) {
            rows = rows.subList(0, pageSize);
        }

        List<MessageItem> items = rows.stream()
                .map(MessageItem::toMessageItem)
                .toList();

        String nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            IssueMessage last = rows.get(rows.size() - 1);
            nextCursor = Cursor.encode(objectMapper, new Cursor(last.getCreatedAt(), last.getId()));
        }

        return MessagePageResp.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    @Transactional
    public void updateUserThreadStatus(Long userId, Long threadId, ThreadStatus newStatus) {
        IssueThread thread = threadRepo.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        if (newStatus == null) {
            throw new IllegalArgumentException("status cannot be null");
        }

        thread.setStatus(newStatus);
        threadRepo.save(thread);
    }

    public List<IssueThread> listThreads(
            Long userId,
            int size,
            UserType userType,
            String cursor,
            List<String> statuses) {

        boolean hasStatuses = statuses != null && !statuses.isEmpty();

        if (cursor == null || cursor.isBlank()) {
            if (!hasStatuses) {
                return switch (userType) {
                    case USER -> threadRepo.listUserFirstPage(userId, size);
                    case ADMIN -> threadRepo.listAdminFirstPage(size);
                };
            } else {
                return switch (userType) {
                    case USER -> threadRepo.listUserFirstPageByStatuses(userId, statuses, size);
                    case ADMIN -> threadRepo.listAdminFirstPageByStatuses(statuses, size);
                };
            }
        } else {
            Cursor decodedCursor = Cursor.decode(objectMapper, cursor);

            if (!hasStatuses) {
                return switch (userType) {
                    case USER -> threadRepo.listUserNextPage(
                            userId,
                            decodedCursor.getLastMessageAt(),
                            decodedCursor.getId(),
                            size
                    );
                    case ADMIN -> threadRepo.listAdminNextPage(
                            decodedCursor.getLastMessageAt(),
                            decodedCursor.getId(),
                            size
                    );
                };
            } else {
                return switch (userType) {
                    case USER -> threadRepo.listUserNextPageByStatuses(
                            userId,
                            statuses,
                            decodedCursor.getLastMessageAt(),
                            decodedCursor.getId(),
                            size
                    );
                    case ADMIN -> threadRepo.listAdminNextPageByStatuses(
                            statuses,
                            decodedCursor.getLastMessageAt(),
                            decodedCursor.getId(),
                            size
                    );
                };
            }
        }
    }
}