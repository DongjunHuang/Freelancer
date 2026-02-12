package com.example.auth.domain;

public record VerificationCreatedEvent(Long userId, String email, String token) {
}
