package com.example.exception;

import lombok.Getter;

/**
 * The exection handles information already exists
 * USERNAME_USED
 * EMAIL_USED
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