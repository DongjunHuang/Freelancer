package com.example.issue.infra.jpa;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.issue.domain.IssueThread;
import com.example.issue.domain.ThreadStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IssueThreadRepo extends JpaRepository<IssueThread, Long> {
    /**
     * Fetch the user feedbacks.
     *
     * @param id     the thread ID.
     * @param userId the user id.
     * @return the corresponding user thread object.
     */
    Optional<IssueThread> findByIdAndUserId(Long id, Long userId);

    @Query(value = """
        SELECT *
        FROM issue_thread
        WHERE user_id = :userId
        ORDER BY last_message_at DESC, id DESC
        LIMIT :size
        """, nativeQuery = true)
    List<IssueThread> listUserFirstPage(
            @Param("userId") Long userId,
            @Param("size") int size);

    @Query(value = """
        SELECT *
        FROM issue_thread
        WHERE user_id = :userId
          AND status IN (:statuses)
        ORDER BY last_message_at DESC, id DESC
        LIMIT :size
        """, nativeQuery = true)
    List<IssueThread> listUserFirstPageByStatuses(
            @Param("userId") Long userId,
            @Param("statuses") List<String> statuses,
            @Param("size") int size);

    @Query(value = """
        SELECT *
        FROM issue_thread
        WHERE user_id = :userId
          AND (:statuses IS NULL OR status IN (:statuses))
          AND (
            last_message_at < :cursorTime
            OR (last_message_at = :cursorTime AND id < :cursorId)
          )
        ORDER BY last_message_at DESC, id DESC
        LIMIT :size
        """, nativeQuery = true)
    List<IssueThread> listUserNextPage(
            @Param("userId") Long userId,
            @Param("statuses") List<String> statuses,
            @Param("cursorTime") Instant cursorTime,
            @Param("cursorId") Long cursorId,
            @Param("size") int size);

    @Query(value = """
            SELECT * FROM issue_thread
            WHERE (:status IS NULL OR status = :status)
            ORDER BY last_message_at DESC, id DESC
            LIMIT ？
            """, nativeQuery = true)
    List<IssueThread> listAdminFirstPage(
            @Param("status") ThreadStatus status,
            int size);

    @Query(value = """
            SELECT * FROM issue_thread
            WHERE (:status IS NULL OR status = :status)
              AND (
                last_message_at < :cursorTime
                OR (last_message_at = :cursorTime AND id < :cursorId)
              )
            ORDER BY last_message_at DESC, id DESC
            LIMIT ？
            """, nativeQuery = true)
    List<IssueThread> listAdminNextPage(
            @Param("status") String status,
            @Param("cursorTime") Instant cursorTime,
            @Param("cursorId") Long cursorId,
            int size);
}