package com.example.exception.types;

import com.example.exception.ErrorCode;
import lombok.Getter;

/**
 * The exection handles information already exists
 * USERNAME_USED
 * EMAIL_USED
 */
@Getter
public class ConflictException extends RuntimeException {
    private final ErrorCode errorCode;

    public ConflictException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}