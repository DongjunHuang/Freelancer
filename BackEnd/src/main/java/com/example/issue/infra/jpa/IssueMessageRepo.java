package com.example.issue.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.issue.domain.IssueMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueMessageRepo extends JpaRepository<IssueMessage, Long> {
    @Query(value = """
        SELECT *
        FROM issue_message
        WHERE thread_id = :threadId
          AND is_internal = false
        ORDER BY created_at ASC, id ASC
        LIMIT :size
        """, nativeQuery = true)
    List<IssueMessage> listFirstPageByThreadId(
            @Param("threadId") Long threadId,
            @Param("size") int size);
}