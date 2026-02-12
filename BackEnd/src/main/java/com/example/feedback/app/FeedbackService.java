package com.example.feedback.app;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.feedback.domain.CreateThreadReq;
import com.example.feedback.domain.CursorPageDto;
import com.example.feedback.domain.FeedbackMessage;
import com.example.feedback.domain.FeedbackThread;
import com.example.feedback.domain.PostMessageReq;
import com.example.feedback.domain.SenderType;
import com.example.feedback.domain.ThreadCursor;
import com.example.feedback.domain.ThreadItemDto;
import com.example.feedback.domain.ThreadStatus;
import com.example.feedback.infra.jpa.FeedbackMessageRepo;
import com.example.feedback.infra.jpa.FeedbackThreadRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

        private final FeedbackThreadRepo threadRepo;
        private final FeedbackMessageRepo messageRepo;
        private final ObjectMapper objectMapper;

        /**
         * Create a new feedback thread and append the first mesasge to the thread.
         *
         * @param userId the user id.
         * @param req    the request.
         */
        @Transactional
        public void createThread(Long userId, CreateThreadReq req) {
                var now = Instant.now();

                FeedbackThread thread = FeedbackThread.builder()
                                .userId(userId)
                                .title(req.getTitle())
                                .status(ThreadStatus.WAITING_ADMIN)
                                .lastMessageAt(now)
                                .unreadByAdmin(1)
                                .unreadByUser(0)
                                .build();
                thread = threadRepo.save(thread);

                messageRepo.save(FeedbackMessage.builder()
                                .threadId(thread.getId())
                                .senderType(SenderType.USER)
                                .senderId(userId)
                                .body(req.getMessage())
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
                messageRepo.save(FeedbackMessage.builder()
                                .threadId(threadId)
                                .senderType(SenderType.USER)
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

        @Transactional
        public void postMessageByAdmin(Long adminId, Long threadId, PostMessageReq req) {
                var thread = threadRepo.findById(threadId)
                                .orElseThrow(() -> new RuntimeException("Thread not found"));

                var now = Instant.now();
                messageRepo.save(FeedbackMessage.builder()
                                .threadId(threadId)
                                .senderType(SenderType.ADMIN)
                                .senderId(adminId)
                                .body(req.getBody())
                                .createdAt(now)
                                .isInternal(req.isInternal())
                                .build());

                if (!req.isInternal()) {
                        thread.setStatus(ThreadStatus.WAITING_USER);
                        thread.setUnreadByUser(thread.getUnreadByUser() + 1);
                }
                thread.setLastMessageAt(now);
                threadRepo.save(thread);
        }

        @Transactional
        public void updateMessageStatusByAdmin(Long threadId, ThreadStatus status) {
                var thread = threadRepo.findById(threadId)
                                .orElseThrow(() -> new RuntimeException("Thread not found"));
                thread.setStatus(status);
                threadRepo.save(thread);
        }

        @Transactional(readOnly = true)
        public CursorPageDto<ThreadItemDto> listUserThreads(
                        Long userId,
                        ThreadStatus status,
                        int size,
                        String cursor) {
                List<FeedbackThread> rows = null;
                if (cursor == null || cursor.isBlank()) {
                        rows = threadRepo.listUserFirstPage(userId, status, size);
                } else {
                        var c = ThreadCursor.decode(objectMapper, cursor);
                        rows = threadRepo.listUserNextPage(userId, status, c.getLastMessageAt(), c.getId(), size);
                }

                var items = rows.stream()
                                .map(t -> new ThreadItemDto(t.getId(), t.getTitle(), t.getStatus(),
                                                t.getLastMessageAt(),
                                                t.getUnreadByUser(), t.getUnreadByAdmin()))
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
        /*
         * @Transactional(readOnly = true)
         * public CursorPageDto<ThreadItemDto> listAdminThreads(String status, int size,
         * String cursor) {
         * var rows = (cursor == null || cursor.isBlank())
         * ? threadRepo.listAdminFirstPage(status, size)
         * : {
         * var c = ThreadCursor.decode(objectMapper, cursor);
         * yield threadRepo.listAdminNextPage(status, c.getLastMessageAt(), c.getId(),
         * size);
         * };
         *
         * var items = rows.stream()
         * .map(t -> new ThreadItemDto(t.getId(), t.getTitle(), t.getStatus(),
         * t.getLastMessageAt(),
         * t.getUnreadByUser(), t.getUnreadByAdmin()))
         * .toList();
         *
         * String nextCursor = null;
         * boolean hasMore = rows.size() == size;
         * if (hasMore) {
         * var last = rows.get(rows.size() - 1);
         * nextCursor = ThreadCursor.encode(objectMapper, new
         * ThreadCursor(last.getLastMessageAt(), last.getId()));
         * }
         * return new CursorPageDto<>(items, nextCursor, hasMore);
         * }
         *
         * /*
         *
         * @Transactional(readOnly = true)
         * public ThreadDetailDto getUserThreadDetail(Long userId, Long threadId) {
         * var thread = threadRepo.findByIdAndUserId(threadId, userId)
         * .orElseThrow(() -> new RuntimeException("Thread not found"));
         *
         * var msgs = messageRepo.findByThreadIdOrderByCreatedAtAscIdAsc(threadId)
         * .stream()
         * .filter(m -> !m.isInternal())
         * .map(m -> new MessageDto(m.getId(), m.getSenderType(), m.getSenderId(),
         * m.getBody(), m.getCreatedAt(),
         * m.isInternal()))
         * .collect(Collectors.toList());
         *
         * return new ThreadDetailDto(thread.getId(), thread.getUserId(),
         * thread.getTitle(),
         * thread.getStatus(), thread.getLastMessageAt(), msgs);
         * }
         */

        /*
         * @Transactional(readOnly = true)
         * public ThreadDetailDto getAdminThreadDetail(Long threadId) {
         * var thread = threadRepo.findById(threadId).orElseThrow(() -> new
         * RuntimeException("Thread not found"));
         * var msgs = messageRepo.findByThreadIdOrderByCreatedAtAscIdAsc(threadId)
         * .stream()
         * .map(m -> new MessageDto(m.getId(), m.getSenderType(), m.getSenderId(),
         * m.getBody(), m.getCreatedAt(),
         * m.isInternal()))
         * .toList();
         *
         * return new ThreadDetailDto(thread.getId(), thread.getUserId(),
         * thread.getTitle(),
         * thread.getStatus(), thread.getLastMessageAt(), msgs);
         * }
         */

}