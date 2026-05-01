package com.example.issue.app;

import com.example.auth.domain.user.User;
import com.example.auth.infra.jpa.UserRepo;
import com.example.exception.types.BadRequestException;
import com.example.exception.ErrorCode;
import com.example.exception.types.NotFoundException;
import com.example.issue.domain.*;
import com.example.issue.infra.jpa.IssueMessageRepo;
import com.example.issue.infra.jpa.IssueThreadRepo;
import com.example.notification.NotificationEventPublisher;
import com.example.notification.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {
    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);

    private final NotificationEventPublisher notificationEventPublisher;

    private final IssueThreadRepo threadRepo;
    private final IssueMessageRepo messageRepo;
    private final UserRepo userRepo;

    private final ObjectMapper objectMapper;

    /**
     * Create a new feedback thread and append the first mesasge to the thread.
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
                .isInternal(false)  // TODO: in case if there is any case we need internal
                .build());
    }

    /**
     * Post message to the corresponding thread from user side.
     * @param controlId   the id of whether user or admin.
     * @param threadId the thread id.
     * @param req      the request.
     */
    @Transactional
    public PostMessageResp postMessage(UserType type, Long controlId, Long threadId, PostMessageReq req) {
        IssueThread thread;
        if (type == UserType.USER) {
            thread = threadRepo.findByIdAndUserId(threadId, controlId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        } else {
            thread = threadRepo.findById(threadId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        }
        if (type == UserType.ADMIN) {
            thread.setUnreadByUser(1);
        } else {
            thread.setUnreadByAdmin(1);
        }

        var now = Instant.now();
        IssueMessage message = IssueMessage.builder()
                .threadId(threadId)
                .userType(type)
                .senderId(controlId)
                .body(req.getBody())
                .createdAt(now)
                .isInternal(req.isInternal())
                .build();
        thread.setLastMessageAt(now);
        messageRepo.save(message);
        threadRepo.save(thread);

        NotificationCommand notification = NotificationCommand.builder()
                .recipientId(thread.getUserId())
                .recipientType(toTargetNotificationRecipientType(type))
                .content(req.getBody())
                .title(thread.getTitle())
                .category(NotificationCategory.COMMUNICATION)
                .type(NotificationType.ISSUE_NEW_MESSAGE)
                .sourceType(NotificationSourceType.ISSUE_THREAD)
                .sourceId(null)
                .targetType(NotificationTargetType.ISSUE_THREAD)
                .targetId(threadId)
                .build();

        notificationEventPublisher.publish(notification);
        return PostMessageResp.builder()
                .thread(ThreadItem.from(thread))
                .message(MessageItem.from(message))
                .build();
    }

    private NotificationRecipientType toTargetNotificationRecipientType(UserType userType) {
        return switch (userType) {
            case UserType.ADMIN -> NotificationRecipientType.USER;
            case UserType.USER -> NotificationRecipientType.ADMIN;
            default -> NotificationRecipientType.NONE;
        };
    }

    @Transactional(readOnly = true)
    public CursorPageDto<ThreadItem> getThreads(
            Long userId,
            String status,
            int size,
            String cursor,
            UserType userType) {
        List<String> statuses = mapStatuses(status);
        List<IssueThread> rows = listThreads(userId, size, userType, cursor, statuses);

        if (rows == null) {
            throw new NotFoundException(ErrorCode.NOT_FOUND);
        }

        List<ThreadItem> items = rows.stream()
                .map(ThreadItem::from)
                .toList();

        String nextCursor = null;
        boolean hasMore = rows.size() == size;

        if (hasMore) {
            IssueThread last = rows.get(rows.size() - 1);
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

    public List<String> mapStatuses(String status) {
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

    /**
     * To return the summary of the threads, used by ADMIN.
     * @return the status of the threads returned.
     */
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

    /**
     * To fetch messages for the corresponding thread id.
     * @param userId     the user id, not admin id.
     * @param threadId   the thread id.
     * @param size       the size.
     * @param cursor     the cursor pointing to the next page.
     * @param isInternal whether the message is internal (for now is false)
     * @return the message fetched.
     */
    @Transactional(readOnly = true)
    public MessagePageResp getMessages(UserType userType, Long userId, Long threadId, int size, String cursor, boolean isInternal) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        if (userType == UserType.ADMIN) {
            threadRepo.findById(threadId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        } else {
            threadRepo.findByIdAndUserId(threadId, userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        }

        List<IssueMessage> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = messageRepo.fetchLatestPage(threadId, isInternal, pageSize + 1);
        } else {
            Cursor messageCursor = Cursor.decode(objectMapper, cursor);
            rows = messageRepo.fetchNextPage(
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
                .map(MessageItem::from)
                .toList()
                .reversed();

        String nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            IssueMessage first = rows.get(rows.size() - 1);
            nextCursor = Cursor.encode(objectMapper, new Cursor(first.getCreatedAt(), first.getId()));
        }

        return MessagePageResp.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    /**
     * Update the thread status by user.
     *
     * @param userId    the user id.
     * @param threadId  the thread id.
     * @param newStatus the status.
     */
    @Transactional
    public void updateThreadStatus(UserType userType, Long userId, Long threadId, ThreadStatus newStatus) {
        if (newStatus == null) {
            throw new BadRequestException(ErrorCode.NOT_VALID_PARAMS);
        }
        IssueThread thread = null;

        if (userType == UserType.ADMIN) {
            thread = threadRepo.findById(threadId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        } else {
            thread = threadRepo.findByIdAndUserId(threadId, userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        }

        thread.setStatus(newStatus);
        threadRepo.save(thread);
    }

    /**
     * According to different requirements to fetch list of threads.
     *
     * @param userId   the user id.
     * @param size     the size of the page.
     * @param userType the user type.
     * @param cursor   the cursor.
     * @param statuses the status.
     * @return the threads found.
     */
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

    @Transactional(readOnly = true)
    public MessagePageResp getLatestUserMessages(UserType userType, Long userId, Long threadId, Instant after) {
        if (userType == UserType.ADMIN) {
            threadRepo.findById(threadId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        } else {
            threadRepo.findByIdAndUserId(threadId, userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
        }

        List<IssueMessage> messages = messageRepo.findLatestMessagesAfter(
                threadId,
                after
        );

        List<MessageItem> items = messages.stream()
                .map(MessageItem::from)
                .toList();

        return MessagePageResp.builder()
                .items(items)
                .nextCursor(null)
                .hasMore(false)
                .build();
    }

    @Transactional
    public void markAsRead(UserType userType, Long userId, Long threadId) {
        if (userType == UserType.ADMIN) {
            IssueThread thread = threadRepo.findById(threadId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
            thread.setUnreadByAdmin(0);
        } else {
            IssueThread thread = threadRepo.findByIdAndUserId(threadId, userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));
            thread.setUnreadByUser(0);
        }
    }

    public void fillUserIdFieldForAdmin(List<ThreadItem> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        Set<Long> userIds = list.stream()
                .map(ThreadItem::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, String> userMap = userRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        User::getUsername
                ));

        list.forEach(item -> {
            item.setUsername(
                    userMap.getOrDefault(item.getUserId(), "Unknown User")
            );
        });
    }
}