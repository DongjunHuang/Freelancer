package com.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    EMAIL_USED(
            "EMAIL_USED",
            "Email Already Registered",
            ErrorCategory.CONFLICT,
            HttpStatus.BAD_REQUEST),

    USERNAME_USED("USERNAME_USED",
            "Username Already Taken",
            ErrorCategory.CONFLICT,
            HttpStatus.BAD_REQUEST),


    USER_IS_NOT_PENDING(
            "USER_IS_NOT_PENDING",
            "User Is Not Pending",
            ErrorCategory.BUSINESS_RULE,
            HttpStatus.BAD_REQUEST),

    // Not found exception
    NOT_FOUND(
    "NOT_FOUND",
    "Resource Not Found",
            ErrorCategory.NOT_FOUND,
            HttpStatus.NOT_FOUND),

    USER_NOT_FOUND(
            "USER_NOT_FOUND",
            "User Not Found",
            ErrorCategory.NOT_FOUND,
            HttpStatus.NOT_FOUND),

    DATASET_NOT_FOUND(
            "DATASET_NOT_FOUND",
            "Dataset Not Found",
            ErrorCategory.NOT_FOUND,
            HttpStatus.NOT_FOUND),

    FILE_READ_FAILED(
            "FILE_READ_FAILED",
            "Failed To Read The File",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    NOT_VALID_DATE_COLUMN(
            "NOT_VALID_DATE_COLUMN",
            "Not Able To Find Date Column",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    NOT_VALID_PARAMS(
            "NOT_VALID_PARAMS",
            "Not Valid Parameters",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    NOT_VALID_SYMBOL_COLUMN(
            "NOT_VALID_SYMBOL_COLUMN",
            "Not Able To Find Symbol Column",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    NOT_VALID_REFRESH_TOKEN(
            "NOT_VALID_REFRESH_TOKEN",
            "Not Able To Find Refresh Token",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    DATASET_NAME_USED(
            "DATASET_NAME_USED",
            "The Dataset Is Already Existed.",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    NOT_VALID_FILE(
            "NOT_VALID_FILE",
            "The File Is Not Valid",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    DATASET_NOT_AVAILABLE(
            "DATASET_NOT_AVAILABLE",
            "The Dataset Is Not Available, Please Try Again.",
            ErrorCategory.DATASET_STATUS,
            HttpStatus.INTERNAL_SERVER_ERROR),

    UPLOAD_FAILED(
            "UPLOAD_FAILED",
            "Failed To Upload",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    NOT_VALID_SET_NAME(
            "NOT_VALID_SET_NAME",
            "The Dataset Name Is Not Valid",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    DELETE_FAILED(
            "DELETE_FAILED",
            "Failed To Delete The Dataset",
            ErrorCategory.BAD_REQUEST,
            HttpStatus.BAD_REQUEST),

    // Authentication
    USER_IS_NOT_VERIFIED(
            "USER_IS_NOT_VERIFIED",
            "The user is not verified through email",
            ErrorCategory.AUTHENTICATION,
            HttpStatus.UNAUTHORIZED),

    TOKEN_EXPIRED(
            "TOKEN_EXPIRED",
            "The Token Is Expired.",
            ErrorCategory.BUSINESS_RULE,
            HttpStatus.UNAUTHORIZED),

    TOKEN_INVALID(
            "TOKEN_INVALID",
            "The Token Is Not Valid.",
            ErrorCategory.BUSINESS_RULE,
            HttpStatus.UNAUTHORIZED),

    NOT_VALID_ADMIN(
            "NOT_VALID_ADMIN",
            "The User Is Not Valid Admin",
            ErrorCategory.AUTHENTICATION,
            HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final ErrorCategory category;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, ErrorCategory category, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.category = category;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
