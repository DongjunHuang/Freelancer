package com.example.issue.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.issue.domain.IssueMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface IssueMessageRepo extends JpaRepository<IssueMessage, Long> {
    @Query(value = """
            SELECT *
            FROM issue_message
            WHERE thread_id = :threadId
              AND (:includeInternal = true OR is_internal = false)
            ORDER BY created_at DESC, id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<IssueMessage> fetchLatestPage(
            @Param("threadId") Long threadId,
            @Param("includeInternal") boolean includeInternal,
            @Param("size") int size);

    @Query(value = """
            SELECT *
            FROM issue_message
            WHERE thread_id = :threadId
              AND (:includeInternal = true OR is_internal = false)
              AND (
                created_at < :cursorTime
                OR (created_at = :cursorTime AND id < :cursorId)
              )
            ORDER BY created_at DESC, id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<IssueMessage> fetchNextPage(
            @Param("threadId") Long threadId,
            @Param("includeInternal") boolean includeInternal,
            @Param("cursorTime") Instant cursorTime,
            @Param("cursorId") Long cursorId,
            @Param("size") int size);

    @Query(value = """
            SELECT COUNT(*)
            FROM issue_message
            WHERE thread_id = :threadId
            """, nativeQuery = true)
    long countByThreadId(@Param("threadId") Long threadId);

    @Query("""
                SELECT m
                FROM IssueMessage m
                WHERE m.threadId = :threadId
                  AND m.createdAt > :after
                ORDER BY m.createdAt ASC, m.id ASC
            """)
    List<IssueMessage> findLatestMessagesAfter(
            @Param("threadId") Long threadId,
            @Param("after") Instant after
    );

}