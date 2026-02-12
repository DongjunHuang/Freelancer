package com.example.feedback.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.feedback.domain.FeedbackMessage;

import java.util.List;

public interface FeedbackMessageRepo extends JpaRepository<FeedbackMessage, Long> {
    List<FeedbackMessage> findByThreadIdOrderByCreatedAtAscIdAsc(Long threadId);
}