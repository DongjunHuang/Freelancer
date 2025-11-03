package com.example.exception;

public class ConflictException extends AppException {          // 409
    public ConflictException(String msg) { super("CONFLICT", msg); }
}