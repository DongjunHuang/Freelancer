package com.example.exception;

public class BusinessRuleException extends AppException {      // 422
    public BusinessRuleException(String msg) { super("BUSINESS_RULE", msg); }
}