package com.example.exception;

import lombok.Getter;

/**
 * The resource not found exception including
 * 1. The user not found.
 */
@Getter
public class NotFoundException extends RuntimeException {
    private final ErrorCode error;

    public NotFoundException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    public String getCode() {
        return error.getCode();
    }
}