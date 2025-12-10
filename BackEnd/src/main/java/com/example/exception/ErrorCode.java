package com.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EMAIL_USED("EMAIL_USED", "Email already registered"),
    USERNAME_USED("USERNAME_USED", "Username already taken"),
    EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", "Email not verified"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Verification token expired"),
    PASSWORD_TOO_SHORT("PASSWORD_TOO_SHORT", "Password length must be >= 6"),
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    USER_IS_NOT_PENDING("USER_IS_NOT_PENDING", "User is not pending");

    private final String code;
    private final String message;
}
