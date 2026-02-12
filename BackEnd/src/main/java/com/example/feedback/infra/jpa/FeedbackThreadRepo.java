package com.example.feedback.infra.jpa;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.feedback.domain.FeedbackThread;
import com.example.feedback.domain.ThreadStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FeedbackThreadRepo extends JpaRepository<FeedbackThread, Long> {
  /**
   * Fetch the user feedbacks.
   *
   * @param id     the thread ID.
   * @param userId the user id.
   * @return the corresponding user thread object.
   */
  Optional<FeedbackThread> findByIdAndUserId(Long id, Long userId);

  /**
   * List user the first page.
   *
   * @param userId the user id.
   * @param size   the size to be fetched.
   * @return the list of the feedback thread in the database.
   */
  @Query(value = """
      SELECT *
      FROM feedback_thread
      WHERE user_id = :userId
          AND (:status IS NULL OR status = :status)
      ORDER BY last_message_at DESC, id DESC
      LIMIT :size
      """, nativeQuery = true)
  List<FeedbackThread> listUserFirstPage(
      @Param("userId") Long userId,
      @Param("status") ThreadStatus status,
      @Param("size") int size);

  @Query(value = """
      SELECT * FROM feedback_thread
      WHERE user_id = :userId
        AND (:status IS NULL OR status = :status)
        AND (
          last_message_at < :cursorTime
          OR (last_message_at = :cursorTime AND id < :cursorId)
        )
      ORDER BY last_message_at DESC, id DESC
      LIMIT :size
      """, nativeQuery = true)
  List<FeedbackThread> listUserNextPage(
      @Param("userId") Long userId,
      @Param("status") ThreadStatus status,
      @Param("cursorTime") Instant cursorTime,
      @Param("cursorId") Long cursorId,
      @Param("size") int size);

  // TODO:============================================================================================================================

  @Query(value = """
      SELECT * FROM feedback_thread
      WHERE (:status IS NULL OR status = :status)
      ORDER BY last_message_at DESC, id DESC
      LIMIT ？
      """, nativeQuery = true)
  List<FeedbackThread> listAdminFirstPage(
      @Param("status") ThreadStatus status,
      int size);

  @Query(value = """
      SELECT * FROM feedback_thread
      WHERE (:status IS NULL OR status = :status)
        AND (
          last_message_at < :cursorTime
          OR (last_message_at = :cursorTime AND id < :cursorId)
        )
      ORDER BY last_message_at DESC, id DESC
      LIMIT ？
      """, nativeQuery = true)
  List<FeedbackThread> listAdminNextPage(
      @Param("status") String status,
      @Param("cursorTime") Instant cursorTime,
      @Param("cursorId") Long cursorId,
      int size);
}