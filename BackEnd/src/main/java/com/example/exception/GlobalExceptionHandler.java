package com.example.exception;

import com.example.exception.types.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResp> handleConflictException(ConflictException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResp> handleAuthenticationException(AuthenticationException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResp> handleBadRequestException(BadRequestException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResp> handleBusinessRuleException(BusinessRuleException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(DatasetStatusException.class)
    public ResponseEntity<ErrorResp> handleDatasetStatusException(DatasetStatusException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResp> handleNotFound(NotFoundException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResp> handleUnknownException(Exception ex) {
        ErrorResp body = new ErrorResp(
                "INTERNAL_ERROR",
                "Unexpected server error",
                ErrorCategory.UNKNOWN.name(),
                java.time.Instant.now()
        );

        return ResponseEntity.internalServerError().body(body);
    }

    private ResponseEntity<ErrorResp> buildResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResp.from(errorCode));
    }
}