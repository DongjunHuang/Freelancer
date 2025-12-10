package com.example.exception;

import lombok.Getter;

/**
 * The exection handles information already exists
 * 1. User name exists when user signs up
 * 2. User email address exists when user signs up
 */
@Getter
public class ConflictException extends RuntimeException {
    private final ErrorCode error;

    public ConflictException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    public String getCode() {
        return error.getCode();
    }
}