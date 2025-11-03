package com.example.exception;

public class NotFoundException extends AppException {          // 404
    public NotFoundException(String msg) { super("NOT_FOUND", msg); }
}