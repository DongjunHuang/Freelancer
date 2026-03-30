package com.example.exception.types;

import com.example.exception.ErrorCode;
import lombok.Getter;

/**
 * The dataset status exception,
 * USERNAME_USED
 * EMAIL_USED
 */
@Getter
public class DatasetStatusException extends RuntimeException {
    private final ErrorCode errorCode;

    public DatasetStatusException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}