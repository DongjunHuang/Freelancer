package com.example.issue.app;

import com.example.exception.ErrorCode;
import com.example.exception.NotFoundException;
import com.example.issue.domain.admin.AdminThreadItemDto;
import com.example.issue.domain.admin.AdminThreadPageResp;
import com.example.issue.domain.admin.AdminThreadStatsResp;
import com.example.issue.domain.common.*;
import com.example.issue.domain.user.*;
import com.example.issue.infra.jpa.IssueMessageRepo;
import com.example.issue.infra.jpa.IssueThreadRepo;
import com.example.security.JwtUserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminIssueService {
    private final IssueThreadRepo threadRepo;
    private final IssueMessageRepo messageRepo;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AdminThreadPageResp getAdminThreads(String status, int size, String cursor) {
        List<String> statuses = mapStatuses(status);

        List<IssueThread> rows = null;

        if (cursor == null || cursor.isBlank()) {
            rows = (statuses == null || statuses.isEmpty())
                    ? threadRepo.listAdminFirstPage(size + 1)
                    : threadRepo.listAdminFirstPageByStatuses(statuses, size + 1);
        } else {
            MessageCursor c = decodeCursor(cursor);

            rows = (statuses == null || statuses.isEmpty())
                    ? threadRepo.listAdminNextPage(c.lastMessageAt(), c.id(), size + 1)
                    : threadRepo.listAdminNextPageByStatuses(statuses, c.lastMessageAt(), c.id(), size + 1);
        }

        boolean hasMore = rows.size() > size;
        if (hasMore) {
            rows = rows.subList(0, size);
        }

        List<AdminThreadItemDto> items = rows.stream()
                .map(this::toAdminThreadItemDto)
                .toList();

        String nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            IssueThread last = rows.get(rows.size() - 1);
            nextCursor = encodeCursor(last.getLastMessageAt(), last.getId());
        }

        return AdminThreadPageResp.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    private AdminThreadItemDto toAdminThreadItemDto(IssueThread t) {
        return AdminThreadItemDto.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .title(t.getTitle())
                .status(t.getStatus())
                .type(t.getIssueType())
                .createdAt(t.getCreatedAt())
                .lastMessageAt(t.getLastMessageAt())
                .unreadByAdmin(t.getUnreadByAdmin())
                .unreadByUser(t.getUnreadByUser())
                .build();
    }

    private List<String> mapStatuses(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return switch (status.toUpperCase()) {
            case "OPEN" -> List.of(
                    ThreadStatus.WAITING_USER.name(),
                    ThreadStatus.WAITING_ADMIN.name()
            );
            case "WAITING_USER" -> List.of(ThreadStatus.WAITING_USER.name());
            case "WAITING_ADMIN" -> List.of(ThreadStatus.WAITING_ADMIN.name());
            case "RESOLVED" -> List.of(ThreadStatus.RESOLVED.name());
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }

    private String encodeCursor(Instant lastMessageAt, Long id) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "lastMessageAt", lastMessageAt.toString(),
                    "id", id
            ));
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode cursor", e);
        }
    }

    private MessageCursor decodeCursor(String cursor) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(json);
            Instant lastMessageAt = Instant.parse(node.get("lastMessageAt").asText());
            long id = node.get("id").asLong();
            return new MessageCursor(lastMessageAt, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }

    private record MessageCursor(Instant lastMessageAt, Long id) {}

    @Transactional(readOnly = true)
    public AdminThreadStatsResp getThreadStats(Long adminId) {
        long all = threadRepo.countAllThreads();
        long waitingAdmin = threadRepo.countByStatus(ThreadStatus.WAITING_ADMIN.name());
        long waitingUser = threadRepo.countByStatus(ThreadStatus.WAITING_USER.name());
        long resolved = threadRepo.countByStatus(ThreadStatus.RESOLVED.name());

        return AdminThreadStatsResp.builder()
                .all(all)
                .waitingAdmin(waitingAdmin)
                .waitingUser(waitingUser)
                .resolved(resolved)
                .open(waitingAdmin + waitingUser)
                .build();
    }

    @Transactional
    public void postMessageByAdmin(Long adminId, Long threadId, PostMessageReq req) {
        var thread = threadRepo.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        var now = Instant.now();
        messageRepo.save(IssueMessage.builder()
                .threadId(threadId)
                .userType(UserType.ADMIN)
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
    public MessagePageResp getAdminThreadMessages(Long userId, Long threadId, int size) {
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
}