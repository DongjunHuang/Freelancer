package com.example.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The mail token repo to issue mail token to user.
 */
public interface MailTokenRepo extends JpaRepository<MailToken,Long> {
    Optional<MailToken> findByToken(String token);
    void deleteByEmail(String email);
}