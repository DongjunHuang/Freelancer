package com.example.exception;

import lombok.Getter;

/**
 * The business rules exceptions.
 * USER_IS_NOT_PENDING
 * TOKEN_EXPIRED
 * TOKEN_INVALID
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