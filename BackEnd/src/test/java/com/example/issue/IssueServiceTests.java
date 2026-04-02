package com.example.issue;

import com.example.exception.types.BadRequestException;
import com.example.exception.types.NotFoundException;
import com.example.issue.app.IssueService;
import com.example.issue.domain.*;
import com.example.issue.infra.jpa.IssueMessageRepo;
import com.example.issue.infra.jpa.IssueThreadRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTests {

    @Mock
    private IssueThreadRepo threadRepo;

    @Mock
    private IssueMessageRepo messageRepo;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    @Spy
    private IssueService issueThreadService;

    @Test
    void postMessageShouldSaveMessageAndUpdateThreadWhenUserTypeIsUser() {
        Long userId = 1L;
        Long threadId = 100L;

        PostMessageReq req = new PostMessageReq();
        req.setBody("hello from user");
        req.setInternal(false);

        IssueThread thread = new IssueThread();
        thread.setId(threadId);
        thread.setUserId(userId);

        when(threadRepo.findByIdAndUserId(threadId, userId)).thenReturn(Optional.of(thread));

        issueThreadService.postMessage(UserType.USER, userId, threadId, req);

        ArgumentCaptor<IssueMessage> messageCaptor = ArgumentCaptor.forClass(IssueMessage.class);
        verify(messageRepo).save(messageCaptor.capture());

        IssueMessage savedMessage = messageCaptor.getValue();
        assertEquals(threadId, savedMessage.getThreadId());
        assertEquals(UserType.USER, savedMessage.getUserType());
        assertEquals(userId, savedMessage.getSenderId());
        assertEquals("hello from user", savedMessage.getBody());
        assertFalse(savedMessage.isInternal());
        assertNotNull(savedMessage.getCreatedAt());

        verify(threadRepo).findByIdAndUserId(threadId, userId);
        verify(threadRepo).save(thread);

        assertNotNull(thread.getLastMessageAt());
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void postMessageShouldSaveMessageAndUpdateThreadWhenUserTypeIsAdmin() {
        Long userId = 999L;
        Long threadId = 200L;

        PostMessageReq req = new PostMessageReq();
        req.setBody("hello from admin");
        req.setInternal(true);

        IssueThread thread = new IssueThread();
        thread.setId(threadId);

        when(threadRepo.findById(threadId)).thenReturn(Optional.of(thread));

        issueThreadService.postMessage( UserType.ADMIN ,null, threadId, req);

        ArgumentCaptor<IssueMessage> messageCaptor = ArgumentCaptor.forClass(IssueMessage.class);
        verify(messageRepo).save(messageCaptor.capture());

        IssueMessage savedMessage = messageCaptor.getValue();
        assertEquals(threadId, savedMessage.getThreadId());
        assertEquals(UserType.ADMIN, savedMessage.getUserType());
        assertEquals("hello from admin", savedMessage.getBody());
        assertTrue(savedMessage.isInternal());
        assertNotNull(savedMessage.getCreatedAt());

        verify(threadRepo).findById(threadId);
        verify(threadRepo).save(thread);

        assertNotNull(thread.getLastMessageAt());
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void postMessageShouldThrowNotFoundExceptionWhenUserThreadNotFound() {
        Long userId = 1L;
        Long threadId = 100L;

        PostMessageReq req = new PostMessageReq();
        req.setBody("hello");
        req.setInternal(false);

        when(threadRepo.findByIdAndUserId(threadId, userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> issueThreadService.postMessage(UserType.USER, userId, threadId, req)
        );

        verify(threadRepo).findByIdAndUserId(threadId, userId);
        verify(messageRepo, never()).save(any());
        verify(threadRepo, never()).save(any(IssueThread.class));
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void postMessageShouldThrowNotFoundExceptionWhenAdminThreadNotFound() {
        Long userId = 999L;
        Long threadId = 200L;

        PostMessageReq req = new PostMessageReq();
        req.setBody("hello");
        req.setInternal(true);

        when(threadRepo.findById(threadId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> issueThreadService.postMessage(UserType.ADMIN, null, threadId, req)
        );

        verify(threadRepo).findById(threadId);
        verify(messageRepo, never()).save(any());
        verify(threadRepo, never()).save(any(IssueThread.class));
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void getThreadsShouldReturnCursorPageDtoWhenRowsExistAndHasNoMore() {
        Long userId = 1L;
        String status = "OPEN";
        int size = 3;
        String cursor = null;
        UserType userType = UserType.USER;

        List<String> statuses = List.of("WAITING_ADMIN", "WAITING_USER");

        IssueThread t1 = buildThread(101L, Instant.parse("2026-03-23T10:00:00Z"));
        IssueThread t2 = buildThread(102L, Instant.parse("2026-03-23T09:00:00Z"));
        List<IssueThread> rows = List.of(t1, t2);

        doReturn(statuses).when(issueThreadService).mapStatuses(status);
        doReturn(rows).when(issueThreadService).listThreads(userId, size, userType, cursor, statuses);

        CursorPageDto<ThreadItem> result = issueThreadService.getThreads(
                userId, status, size, cursor, userType
        );

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());

        verify(issueThreadService).mapStatuses(status);
        verify(issueThreadService).listThreads(userId, size, userType, cursor, statuses);
    }

    @Test
    void getThreads_shouldReturnHasMoreTrueAndNextCursor_whenRowsSizeEqualsSize() {
        Long userId = 1L;
        String status = "OPEN";
        int size = 2;
        String cursor = "abc";
        UserType userType = UserType.ADMIN;

        List<String> statuses = List.of("WAITING_ADMIN");

        IssueThread t1 = buildThread(101L, Instant.parse("2026-03-23T10:00:00Z"));
        IssueThread t2 = buildThread(102L, Instant.parse("2026-03-23T09:00:00Z"));
        List<IssueThread> rows = List.of(t1, t2);

        doReturn(statuses).when(issueThreadService).mapStatuses(status);
        doReturn(rows).when(issueThreadService).listThreads(userId, size, userType, cursor, statuses);

        try (MockedStatic<Cursor> mockedCursor = mockStatic(Cursor.class)) {
            mockedCursor.when(() -> Cursor.encode(eq(objectMapper), any(Cursor.class)))
                    .thenReturn("next-cursor");

            CursorPageDto<ThreadItem> result = issueThreadService.getThreads(
                    userId, status, size, cursor, userType
            );

            assertNotNull(result);
            assertEquals(2, result.getItems().size());
            assertTrue(result.isHasMore());
            assertEquals("next-cursor", result.getNextCursor());

            verify(issueThreadService).mapStatuses(status);
            verify(issueThreadService).listThreads(userId, size, userType, cursor, statuses);
            mockedCursor.verify(() -> Cursor.encode(eq(objectMapper), any(Cursor.class)));
        }
    }

    @Test
    void getThreads_shouldThrowNotFoundException_whenRowsIsNull() {
        Long userId = 1L;
        String status = "OPEN";
        int size = 2;
        String cursor = null;
        UserType userType = UserType.USER;

        List<String> statuses = List.of("WAITING_ADMIN");

        doReturn(statuses).when(issueThreadService).mapStatuses(status);
        doReturn(null).when(issueThreadService).listThreads(userId, size, userType, cursor, statuses);

        assertThrows(NotFoundException.class, () ->
                issueThreadService.getThreads(userId, status, size, cursor, userType)
        );

        verify(issueThreadService).mapStatuses(status);
        verify(issueThreadService).listThreads(userId, size, userType, cursor, statuses);
    }

    @Test
    void getThreads_shouldPassMappedStatusesToListThreads() {
        Long userId = 99L;
        String status = "RESOLVED";
        int size = 10;
        String cursor = "cursor-value";
        UserType userType = UserType.ADMIN;

        List<String> mappedStatuses = List.of("RESOLVED");
        List<IssueThread> rows = List.of();

        doReturn(mappedStatuses).when(issueThreadService).mapStatuses(status);
        doReturn(rows).when(issueThreadService).listThreads(userId, size, userType, cursor, mappedStatuses);

        CursorPageDto<ThreadItem> result = issueThreadService.getThreads(
                userId, status, size, cursor, userType
        );

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());

        verify(issueThreadService).mapStatuses(status);
        verify(issueThreadService).listThreads(userId, size, userType, cursor, mappedStatuses);
    }

    @Test
    void getThreads_shouldReturnEmptyItems_whenRowsEmpty() {
        Long userId = 1L;
        String status = null;
        int size = 20;
        String cursor = null;
        UserType userType = UserType.USER;

        List<String> statuses = List.of();

        doReturn(statuses).when(issueThreadService).mapStatuses(status);
        doReturn(List.of()).when(issueThreadService).listThreads(userId, size, userType, cursor, statuses);

        CursorPageDto<ThreadItem> result = issueThreadService.getThreads(
                userId, status, size, cursor, userType
        );

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());
    }

    @Test
    void getThreadStatsShouldReturnCorrectStats() {
        when(threadRepo.countAllThreads()).thenReturn(20L);
        when(threadRepo.countByStatus(ThreadStatus.WAITING_ADMIN.name())).thenReturn(5L);
        when(threadRepo.countByStatus(ThreadStatus.WAITING_USER.name())).thenReturn(7L);
        when(threadRepo.countByStatus(ThreadStatus.RESOLVED.name())).thenReturn(8L);

        ThreadStatsResp result = issueThreadService.getThreadStats();

        assertEquals(20L, result.getAll());
        assertEquals(5L, result.getWaitingAdmin());
        assertEquals(7L, result.getWaitingUser());
        assertEquals(8L, result.getResolved());
        assertEquals(12L, result.getOpen());

        verify(threadRepo).countAllThreads();
        verify(threadRepo).countByStatus(ThreadStatus.WAITING_ADMIN.name());
        verify(threadRepo).countByStatus(ThreadStatus.WAITING_USER.name());
        verify(threadRepo).countByStatus(ThreadStatus.RESOLVED.name());
        verifyNoMoreInteractions(threadRepo);
    }

    @Test
    void getMessagesShouldReturnFirstPageForAdminWhenCursorIsBlank() {
        Long threadId = 10L;
        int size = 20;
        boolean isInternal = false;

        when(threadRepo.findById(threadId)).thenReturn(Optional.of(new IssueThread()));

        IssueMessage m1 = buildMessage(1L, Instant.parse("2026-03-23T10:00:00Z"));
        IssueMessage m2 = buildMessage(2L, Instant.parse("2026-03-23T10:01:00Z"));

        when(messageRepo.fetchLatestPage(threadId, isInternal, size + 1))
                .thenReturn(List.of(m1, m2));

        MessagePageResp result = issueThreadService.getMessages(
                UserType.ADMIN,
                null,
                threadId,
                size,
                null,
                isInternal
        );

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());

        verify(threadRepo).findById(threadId);
        verify(messageRepo).fetchLatestPage(threadId, isInternal, size + 1);
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void getMessagesShouldReturnFirstPageForUserWhenCursorIsBlank() {
        Long userId = 100L;
        Long threadId = 10L;
        int size = 20;
        boolean isInternal = false;

        when(threadRepo.findByIdAndUserId(threadId, userId)).thenReturn(Optional.of(new IssueThread()));

        IssueMessage m1 = buildMessage(1L, Instant.parse("2026-03-23T10:00:00Z"));
        when(messageRepo.fetchLatestPage(threadId, isInternal, size + 1))
                .thenReturn(List.of(m1));

        MessagePageResp result = issueThreadService.getMessages(
                UserType.USER,
                userId,
                threadId,
                size,
                "",
                isInternal
        );

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());

        verify(threadRepo).findByIdAndUserId(threadId, userId);
        verify(messageRepo).fetchLatestPage(threadId, isInternal, size + 1);
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void getMessagesShouldReturnNextPageWhenCursorProvided() {
        Long userId = 100L;
        Long threadId = 10L;
        int size = 20;
        boolean isInternal = true;
        String cursor = "encoded-cursor";

        when(threadRepo.findByIdAndUserId(threadId, userId)).thenReturn(Optional.of(new IssueThread()));

        Cursor decodedCursor = mock(Cursor.class);
        Instant lastMessageAt = Instant.parse("2026-03-23T09:59:00Z");
        Long lastId = 99L;

        when(decodedCursor.getLastMessageAt()).thenReturn(lastMessageAt);
        when(decodedCursor.getId()).thenReturn(lastId);

        IssueMessage m1 = buildMessage(101L, Instant.parse("2026-03-23T10:00:00Z"));
        IssueMessage m2 = buildMessage(102L, Instant.parse("2026-03-23T10:01:00Z"));

        when(messageRepo.fetchNextPage(threadId, isInternal, lastMessageAt, lastId, size + 1))
                .thenReturn(List.of(m1, m2));

        try (MockedStatic<Cursor> mockedCursor = mockStatic(Cursor.class)) {
            mockedCursor.when(() -> Cursor.decode(objectMapper, cursor)).thenReturn(decodedCursor);

            MessagePageResp result = issueThreadService.getMessages(
                    UserType.USER,
                    userId,
                    threadId,
                    size,
                    cursor,
                    isInternal
            );

            assertNotNull(result);
            assertEquals(2, result.getItems().size());
            assertFalse(result.isHasMore());
            assertNull(result.getNextCursor());

            mockedCursor.verify(() -> Cursor.decode(objectMapper, cursor));
            verify(threadRepo).findByIdAndUserId(threadId, userId);
            verify(messageRepo).fetchNextPage(threadId, isInternal, lastMessageAt, lastId, size + 1);
            verifyNoMoreInteractions(threadRepo, messageRepo);
        }
    }

    @Test
    void getMessagesShouldSetHasMoreAndNextCursorWhenReturnedRowsExceedPageSize() {
        Long threadId = 10L;
        int size = 2;
        boolean isInternal = false;

        when(threadRepo.findById(threadId)).thenReturn(Optional.of(new IssueThread()));

        IssueMessage m1 = buildMessage(1L, Instant.parse("2026-03-23T10:00:00Z"));
        IssueMessage m2 = buildMessage(2L, Instant.parse("2026-03-23T10:01:00Z"));
        IssueMessage m3 = buildMessage(3L, Instant.parse("2026-03-23T10:02:00Z"));

        when(messageRepo.fetchLatestPage(threadId, isInternal, size + 1))
                .thenReturn(List.of(m1, m2, m3));

        String encodedNextCursor = "next-cursor";

        try (MockedStatic<Cursor> mockedCursor = mockStatic(Cursor.class)) {
            mockedCursor.when(() -> Cursor.encode(eq(objectMapper), any(Cursor.class)))
                    .thenReturn(encodedNextCursor);

            MessagePageResp result = issueThreadService.getMessages(
                    UserType.ADMIN,
                    null,
                    threadId,
                    size,
                    null,
                    isInternal
            );

            assertNotNull(result);
            assertEquals(2, result.getItems().size());
            assertTrue(result.isHasMore());
            assertEquals(encodedNextCursor, result.getNextCursor());

            verify(threadRepo).findById(threadId);
            verify(messageRepo).fetchLatestPage(threadId, isInternal, size + 1);

            mockedCursor.verify(() -> Cursor.encode(eq(objectMapper), any(Cursor.class)));
            verifyNoMoreInteractions(threadRepo, messageRepo);
        }
    }

    @Test
    void getMessagesShouldNotSetNextCursorWhenNoMoreData() {
        Long threadId = 10L;
        int size = 2;
        boolean isInternal = false;

        when(threadRepo.findById(threadId)).thenReturn(Optional.of(new IssueThread()));

        IssueMessage m1 = buildMessage(1L, Instant.parse("2026-03-23T10:00:00Z"));
        IssueMessage m2 = buildMessage(2L, Instant.parse("2026-03-23T10:01:00Z"));

        when(messageRepo.fetchLatestPage(threadId, isInternal, size + 1))
                .thenReturn(List.of(m1, m2));

        MessagePageResp result = issueThreadService.getMessages(
                UserType.ADMIN,
                null,
                threadId,
                size,
                null,
                isInternal
        );

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());

        verify(threadRepo).findById(threadId);
        verify(messageRepo).fetchLatestPage(threadId, isInternal, size + 1);
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void getMessagesShouldThrowNotFoundExceptionWhenAdminThreadNotFound() {
        Long threadId = 10L;

        when(threadRepo.findById(threadId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> issueThreadService.getMessages(
                        UserType.ADMIN,
                        null,
                        threadId,
                        20,
                        null,
                        false
                )
        );

        verify(threadRepo).findById(threadId);
        verify(messageRepo, never()).fetchLatestPage(anyLong(), anyBoolean(), anyInt());
        verify(messageRepo, never()).fetchNextPage(anyLong(), anyBoolean(), any(), anyLong(), anyInt());
    }

    @Test
    void getMessagesShouldThrowNotFoundExceptionWhenUserThreadNotFound() {
        Long userId = 100L;
        Long threadId = 10L;

        when(threadRepo.findByIdAndUserId(threadId, userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> issueThreadService.getMessages(
                        UserType.USER,
                        userId,
                        threadId,
                        20,
                        null,
                        false
                )
        );

        verify(threadRepo).findByIdAndUserId(threadId, userId);
        verify(messageRepo, never()).fetchLatestPage(anyLong(), anyBoolean(), anyInt());
        verify(messageRepo, never()).fetchNextPage(anyLong(), anyBoolean(), any(), anyLong(), anyInt());
    }

    @Test
    void getMessagesShouldClampSizeToMinOne() {
        Long threadId = 10L;
        boolean isInternal = false;

        when(threadRepo.findById(threadId)).thenReturn(Optional.of(new IssueThread()));
        when(messageRepo.fetchLatestPage(threadId, isInternal, 2))
                .thenReturn(List.of());

        MessagePageResp result = issueThreadService.getMessages(
                UserType.ADMIN,
                null,
                threadId,
                0,
                null,
                isInternal
        );

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());

        verify(threadRepo).findById(threadId);
        verify(messageRepo).fetchLatestPage(threadId, isInternal, 2);
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void getMessagesShouldClampSizeToMaxFifty() {
        Long threadId = 10L;
        boolean isInternal = false;

        when(threadRepo.findById(threadId)).thenReturn(Optional.of(new IssueThread()));
        when(messageRepo.fetchLatestPage(threadId, isInternal, 51))
                .thenReturn(List.of());

        MessagePageResp result = issueThreadService.getMessages(
                UserType.ADMIN,
                null,
                threadId,
                999,
                null,
                isInternal
        );

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertFalse(result.isHasMore());
        assertNull(result.getNextCursor());

        verify(threadRepo).findById(threadId);
        verify(messageRepo).fetchLatestPage(threadId, isInternal, 51);
        verifyNoMoreInteractions(threadRepo, messageRepo);
    }

    @Test
    void updateUserThreadStatusShouldUpdateStatusWhenThreadExistsAndStatusValid() {
        Long userId = 1L;
        Long threadId = 100L;
        ThreadStatus newStatus = ThreadStatus.RESOLVED;

        IssueThread thread = new IssueThread();
        thread.setId(threadId);
        thread.setUserId(userId);
        thread.setStatus(ThreadStatus.WAITING_ADMIN);

        when(threadRepo.findByIdAndUserId(threadId, userId)).thenReturn(Optional.of(thread));
        when(threadRepo.save(thread)).thenReturn(thread);

        issueThreadService.updateThreadStatus(UserType.USER, userId, threadId, newStatus);

        assertEquals(ThreadStatus.RESOLVED, thread.getStatus());
        verify(threadRepo).findByIdAndUserId(threadId, userId);
        verify(threadRepo).save(thread);
        verifyNoMoreInteractions(threadRepo);
    }

    @Test
    void updateUserThreadStatus_shouldThrowNotFoundException_whenThreadDoesNotExist() {
        Long userId = 1L;
        Long threadId = 100L;

        when(threadRepo.findByIdAndUserId(threadId, userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> issueThreadService.updateThreadStatus(UserType.USER, userId, threadId, ThreadStatus.RESOLVED)
        );

        verify(threadRepo).findByIdAndUserId(threadId, userId);
        verify(threadRepo, never()).save(any());
        verifyNoMoreInteractions(threadRepo);
    }

    @Test
    void updateUserThreadStatusShouldThrowBadRequestExceptionWhenNewStatusIsNull() {
        Long userId = 1L;
        Long threadId = 100L;
        assertThrows(
                BadRequestException.class,
                () -> issueThreadService.updateThreadStatus(UserType.USER, userId, threadId, null)
        );
    }

    @Test
    void listThreadsShouldCallListUserFirstPageWhenCursorBlankAndStatusesEmpty() {
        Long userId = 1L;
        int size = 20;
        List<IssueThread> expected = List.of(new IssueThread());

        when(threadRepo.listUserFirstPage(userId, size)).thenReturn(expected);

        List<IssueThread> result = issueThreadService.listThreads(
                userId,
                size,
                UserType.USER,
                null,
                null
        );

        assertSame(expected, result);
        verify(threadRepo).listUserFirstPage(userId, size);
        verifyNoMoreInteractions(threadRepo);
    }

    @Test
    void listThreadsShouldCallListAdminFirstPageWhenCursorBlankAndStatusesEmpty() {
        int size = 20;
        List<IssueThread> expected = List.of(new IssueThread());

        when(threadRepo.listAdminFirstPage(size)).thenReturn(expected);

        List<IssueThread> result = issueThreadService.listThreads(
                1L,
                size,
                UserType.ADMIN,
                "",
                List.of()
        );

        assertSame(expected, result);
        verify(threadRepo).listAdminFirstPage(size);
        verifyNoMoreInteractions(threadRepo);
    }

    @Test
    void listThreadsShouldCallListUserFirstPageByStatusesWhenCursorBlankAndStatusesPresent() {
        Long userId = 1L;
        int size = 20;
        List<String> statuses = List.of("OPEN", "RESOLVED");
        List<IssueThread> expected = List.of(new IssueThread());

        when(threadRepo.listUserFirstPageByStatuses(userId, statuses, size)).thenReturn(expected);

        List<IssueThread> result = issueThreadService.listThreads(
                userId,
                size,
                UserType.USER,
                "   ",
                statuses
        );

        assertSame(expected, result);
        verify(threadRepo).listUserFirstPageByStatuses(userId, statuses, size);
        verifyNoMoreInteractions(threadRepo);
    }

    @Test
    void listThreadsShouldCallListAdminFirstPageByStatusesWhenCursorBlankAndStatusesPresent() {
        int size = 20;
        List<String> statuses = List.of("OPEN", "RESOLVED");
        List<IssueThread> expected = List.of(new IssueThread());

        when(threadRepo.listAdminFirstPageByStatuses(statuses, size)).thenReturn(expected);

        List<IssueThread> result = issueThreadService.listThreads(
                1L,
                size,
                UserType.ADMIN,
                null,
                statuses
        );

        assertSame(expected, result);
        verify(threadRepo).listAdminFirstPageByStatuses(statuses, size);
        verifyNoMoreInteractions(threadRepo);
    }

    @Test
    void listThreadsShouldCallListUserNextPageWhenCursorPresentAndStatusesEmpty() {
        Long userId = 1L;
        int size = 20;
        String cursor = "encoded-cursor";
        Instant lastMessageAt = Instant.parse("2026-03-23T10:15:30Z");
        Long id = 99L;

        Cursor decodedCursor = mock(Cursor.class);
        when(decodedCursor.getLastMessageAt()).thenReturn(lastMessageAt);
        when(decodedCursor.getId()).thenReturn(id);

        List<IssueThread> expected = List.of(new IssueThread());
        when(threadRepo.listUserNextPage(userId, lastMessageAt, id, size)).thenReturn(expected);

        try (MockedStatic<Cursor> mockedStatic = mockStatic(Cursor.class)) {
            mockedStatic.when(() -> Cursor.decode(objectMapper, cursor)).thenReturn(decodedCursor);

            List<IssueThread> result = issueThreadService.listThreads(
                    userId,
                    size,
                    UserType.USER,
                    cursor,
                    null
            );

            assertSame(expected, result);
            mockedStatic.verify(() -> Cursor.decode(objectMapper, cursor));
            verify(threadRepo).listUserNextPage(userId, lastMessageAt, id, size);
            verifyNoMoreInteractions(threadRepo);
        }
    }

    @Test
    void listThreadsShouldCallListAdminNextPageWhenCursorPresentAndStatusesEmpty() {
        int size = 20;
        String cursor = "encoded-cursor";
        Instant lastMessageAt = Instant.parse("2026-03-23T10:15:30Z");
        Long id = 99L;

        Cursor decodedCursor = mock(Cursor.class);
        when(decodedCursor.getLastMessageAt()).thenReturn(lastMessageAt);
        when(decodedCursor.getId()).thenReturn(id);

        List<IssueThread> expected = List.of(new IssueThread());
        when(threadRepo.listAdminNextPage(lastMessageAt, id, size)).thenReturn(expected);

        try (MockedStatic<Cursor> mockedStatic = mockStatic(Cursor.class)) {
            mockedStatic.when(() -> Cursor.decode(objectMapper, cursor)).thenReturn(decodedCursor);

            List<IssueThread> result = issueThreadService.listThreads(
                    1L,
                    size,
                    UserType.ADMIN,
                    cursor,
                    List.of()
            );

            assertSame(expected, result);
            mockedStatic.verify(() -> Cursor.decode(objectMapper, cursor));
            verify(threadRepo).listAdminNextPage(lastMessageAt, id, size);
            verifyNoMoreInteractions(threadRepo);
        }
    }

    @Test
    void listThreadsShouldCallListUserNextPageByStatusesWhenCursorPresentAndStatusesPresent() {
        Long userId = 1L;
        int size = 20;
        String cursor = "encoded-cursor";
        List<String> statuses = List.of("OPEN");
        Instant lastMessageAt = Instant.parse("2026-03-23T10:15:30Z");
        Long id = 99L;

        Cursor decodedCursor = mock(Cursor.class);
        when(decodedCursor.getLastMessageAt()).thenReturn(lastMessageAt);
        when(decodedCursor.getId()).thenReturn(id);

        List<IssueThread> expected = List.of(new IssueThread());
        when(threadRepo.listUserNextPageByStatuses(userId, statuses, lastMessageAt, id, size))
                .thenReturn(expected);

        try (MockedStatic<Cursor> mockedStatic = mockStatic(Cursor.class)) {
            mockedStatic.when(() -> Cursor.decode(objectMapper, cursor)).thenReturn(decodedCursor);

            List<IssueThread> result = issueThreadService.listThreads(
                    userId,
                    size,
                    UserType.USER,
                    cursor,
                    statuses
            );

            assertSame(expected, result);
            mockedStatic.verify(() -> Cursor.decode(objectMapper, cursor));
            verify(threadRepo).listUserNextPageByStatuses(userId, statuses, lastMessageAt, id, size);
            verifyNoMoreInteractions(threadRepo);
        }
    }

    @Test
    void listThreadsShouldCallListAdminNextPageByStatusesWhenCursorPresentAndStatusesPresent() {
        int size = 20;
        String cursor = "encoded-cursor";
        List<String> statuses = List.of("OPEN");
        Instant lastMessageAt = Instant.parse("2026-03-23T10:15:30Z");
        Long id = 99L;

        Cursor decodedCursor = mock(Cursor.class);
        when(decodedCursor.getLastMessageAt()).thenReturn(lastMessageAt);
        when(decodedCursor.getId()).thenReturn(id);

        List<IssueThread> expected = List.of(new IssueThread());
        when(threadRepo.listAdminNextPageByStatuses(statuses, lastMessageAt, id, size))
                .thenReturn(expected);

        try (MockedStatic<Cursor> mockedStatic = mockStatic(Cursor.class)) {
            mockedStatic.when(() -> Cursor.decode(objectMapper, cursor)).thenReturn(decodedCursor);

            List<IssueThread> result = issueThreadService.listThreads(
                    1L,
                    size,
                    UserType.ADMIN,
                    cursor,
                    statuses
            );

            assertSame(expected, result);
            mockedStatic.verify(() -> Cursor.decode(objectMapper, cursor));
            verify(threadRepo).listAdminNextPageByStatuses(statuses, lastMessageAt, id, size);
            verifyNoMoreInteractions(threadRepo);
        }
    }

    private IssueMessage buildMessage(Long id, Instant createdAt) {
        IssueMessage message = new IssueMessage();
        message.setId(id);
        message.setCreatedAt(createdAt);
        return message;
    }

    private IssueThread buildThread(Long id, Instant lastMessageAt) {
        IssueThread thread = new IssueThread();
        thread.setId(id);
        thread.setLastMessageAt(lastMessageAt);
        return thread;
    }
}