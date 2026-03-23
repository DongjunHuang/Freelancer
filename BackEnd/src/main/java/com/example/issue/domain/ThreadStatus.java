package com.example.issue.domain;

public enum ThreadStatus {
    WAITING_USER, // The issue is open, wait for user to respond.
    WAITING_ADMIN, // The issue is open, wait for admin to respond.
    RESOLVED, // The thread is resolved.
}