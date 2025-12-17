package com.example.exception;

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
    private final ErrorCode error;

    public BadRequestException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

    public String getCode() {
        return error.getCode();
    }
}