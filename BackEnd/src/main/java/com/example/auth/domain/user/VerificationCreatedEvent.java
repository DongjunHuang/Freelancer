package com.example.auth.domain.user;

public record VerificationCreatedEvent(Long userId, String email, String token) {
}
