package com.example.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MailTokenRepo extends JpaRepository<MailToken,Long> {
    Optional<MailToken> findByToken(String token);
    void deleteByUserId(Long userId);
}