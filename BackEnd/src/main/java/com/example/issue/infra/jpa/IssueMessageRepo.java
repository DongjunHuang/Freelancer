package com.example.issue.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.issue.domain.IssueMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IssueMessageRepo extends JpaRepository<IssueMessage, Long> {
    @Query(value = """
        SELECT *
        FROM issue_message
        WHERE thread_id = :threadId
          AND (:includeInternal = true OR is_internal = false)
        ORDER BY created_at ASC, id ASC
        LIMIT :size
        """, nativeQuery = true)
    List<IssueMessage> listFirstPage(
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
    List<IssueMessage> listNextPage(
            @Param("threadId") Long threadId,
            @Param("includeInternal") boolean includeInternal,
            @Param("cursorTime") Instant cursorTime,
            @Param("cursorId") Long cursorId,
            @Param("size") int size);

    @Query(value = """
        SELECT *
        FROM issue_message
        WHERE thread_id = :threadId
        ORDER BY created_at DESC, id DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<IssueMessage> findLatestMessage(
            @Param("threadId") Long threadId);

    @Query(value = """
        SELECT COUNT(*)
        FROM issue_message
        WHERE thread_id = :threadId
        """, nativeQuery = true)
    long countByThreadId(@Param("threadId") Long threadId);
}