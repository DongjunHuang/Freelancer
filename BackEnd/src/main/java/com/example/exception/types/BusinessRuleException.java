package com.example.exception.types;

import com.example.exception.ErrorCode;
import lombok.Getter;

/**
 * The business rules exceptions.
 * USER_IS_NOT_PENDING
 * TOKEN_EXPIRED
 * TOKEN_INVALID
 */
@Getter
public class BusinessRuleException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessRuleException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}