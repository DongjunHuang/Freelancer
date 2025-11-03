package com.example.listeners;

public record VerificationCreatedEvent(Long userId, String email, String token) {}
