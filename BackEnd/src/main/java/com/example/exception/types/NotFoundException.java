package com.example.exception.types;

import com.example.exception.ErrorCode;
import lombok.Getter;

/**
 * The resource not found exception including
 * USER_NOT_FOUND
 */
@Getter
public class NotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}