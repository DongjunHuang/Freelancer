package com.example.exception;

public class BadRequestException extends AppException {        // 400
    public BadRequestException(String msg) { super("BAD_REQUEST", msg); }
}