package com.example.issue.domain;

public enum ThreadStatus {
    OPEN, // Open status contains status outside of RESOLVED
    WAITING_USER, // The issue is open, wait for user to respond.
    WAITING_ADMIN, // The issue is open, wait for admin to respond.
    RESOLVED, // The thread is resolved.
}