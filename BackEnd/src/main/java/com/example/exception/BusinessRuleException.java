package com.example.exception;

import lombok.Getter;

/**
 * The business rules exceptions.
 * 1. The user is not pending.
 */
@Getter
public class BusinessRuleException extends RuntimeException {
    private final ErrorCode error;

    public BusinessRuleException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    public String getCode() {
        return error.getCode();
    }
}