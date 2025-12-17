package com.example.exception;

import lombok.Getter;

/**
 * The dataset status exception,
 * USERNAME_USED
 * EMAIL_USED
 */
@Getter
public class DatasetStatusException extends RuntimeException {
    private final ErrorCode error;

    public DatasetStatusException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    public String getCode() {
        return error.getCode();
    }
}