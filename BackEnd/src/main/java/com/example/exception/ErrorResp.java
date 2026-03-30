package com.example.exception;


import java.time.Instant;

public record ErrorResp(
        String code,
        String message,
        String category,
        Instant timestamp) {
    public static ErrorResp from(ErrorCode errorCode) {
        return new ErrorResp(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getCategory().name(),
                Instant.now()
        );
    }

    public static ErrorResp from(ErrorCode errorCode, String message) {
        return new ErrorResp(
                errorCode.getCode(),
                message,
                errorCode.getCategory().name(),
                Instant.now()
        );
    }
}