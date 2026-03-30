package com.example.exception.types;

import com.example.exception.ErrorCode;
import lombok.Getter;

/**
 * Bad request exception includiong ErrorCode
 * NOT_VALID_FILE
 * NOT_VALID_SET_NAME
 * FILE_READ_FAILED
 * NOT_VALID_DATE_COLUMN
 * NOT_VALID_SYMBOL_COLUMN
 * UPLOAD_FAILED
 * DELETE_FAILED
 */
@Getter
public class BadRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public BadRequestException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}