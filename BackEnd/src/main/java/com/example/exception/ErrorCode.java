package com.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EMAIL_USED("EMAIL_USED", "Email already registered"),
    USERNAME_USED("USERNAME_USED", "Username already taken"),
    EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", "Email not verified"),

    PASSWORD_TOO_SHORT("PASSWORD_TOO_SHORT", "Password length must be >= 6"),

    USER_IS_NOT_PENDING("USER_IS_NOT_PENDING", "User is not pending"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "The token is expired."),
    TOKEN_INVALID("TOKEN_INVALID", "The token is not valid."),

    NOT_VALID_USER("NOT_VALID_USER", "The user is not valid"),

    // Not found exception
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    DATASET_NOT_FOUND("DATASET_NOT_FOUND", "Dataset not found"),

    // Bad request exception
    NOT_VALID_FILE("NOT_VALID_FILE", "The file is not valid"),
    NOT_VALID_SET_NAME("NOT_VALID_SET_NAME", "The dataset name is not valid"),
    FILE_READ_FAILED("FILE_READ_FAILED", "Failed to read the file"),
    NOT_VALID_DATE_COLUMN("NOT_VALID_DATE_COLUMN", "Not able to find date column"),
    NOT_VALID_SYMBOL_COLUMN("NOT_VALID_SYMBOL_COLUMN", "Not able to find symbol column"),

    // Dataset status exception
    DATASET_NOT_AVAILABLE("DATASET_NOT_AVAILABLE", "The dataset is not available, please try again."),

    // Dataset Explainable Code
    UPLOAD_FAILED("UPLOAD_FAILED", "Faile to upload"),
    DELETE_FAILED("DELETE_FAILED", "Faile to delete the dataset");

    private final String code;
    private final String message;
}
