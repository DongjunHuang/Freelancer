package com.example.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    private final ErrorCode error;

    public AuthenticationException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    public String getCode() {
        return error.getCode();
    }
}