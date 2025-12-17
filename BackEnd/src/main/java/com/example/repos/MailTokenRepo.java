package com.example.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The mail token repo to issue mail token to user.
 */
public interface MailTokenRepo extends JpaRepository<MailToken, Long> {
    /**
     * Find mailtoken by token
     * 
     * @param token the token string.
     * 
     * @return the mailtoken object.
     */
    Optional<MailToken> findByToken(String token);

    /**
     * Find mailtoken by user id.
     * 
     * @param userId the user id.
     * 
     * @return the mail token.
     */
    Optional<MailToken> findByUserId(Long userId);

    /**
     * delete mailtoken according to email.
     * 
     * @param email the email address.
     */
    void deleteByEmail(String email);

    /**
     * Delete mailtoken by user name.
     * 
     * @param username the user name.
     */
    void deleteByUsername(String username);
}