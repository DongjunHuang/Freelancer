package com.example.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 统一构造 ProblemDetail（可添加traceId等）
    private ProblemDetail pd(HttpStatus status, String title, String code, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(title);
        p.setProperty("code", code);                           // 你的业务码
        p.setProperty("timestamp", OffsetDateTime.now());
        p.setProperty("traceId", UUID.randomUUID().toString()); // 或从MDC取
        return p;
    }

    // 业务异常映射
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        return pd(HttpStatus.BAD_REQUEST, "Bad Request", ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return pd(HttpStatus.NOT_FOUND, "Not Found", ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleConflict(ConflictException ex) {
        return pd(HttpStatus.CONFLICT, "Conflict", ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleBiz(BusinessRuleException ex) {
        return pd(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getCode(), ex.getMessage());
    }

    // 参数校验：@Valid/@Validated 触发
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalid(MethodArgumentNotValidException ex) {
        var first = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String msg = first.map(f -> f.getField() + " " + f.getDefaultMessage()).orElse("Invalid request");
        ProblemDetail p = pd(HttpStatus.BAD_REQUEST, "Validation Failed", "VALIDATION_ERROR", msg);
        p.setProperty("errors", ex.getBindingResult().getFieldErrors()); // 可裁剪
        return p;
    }

    // Spring Security：未认证/权限不足
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleAuth(AuthenticationException ex) {
        return pd(HttpStatus.UNAUTHORIZED, "Unauthorized", "UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleDenied(AccessDeniedException ex) {
        return pd(HttpStatus.FORBIDDEN, "Forbidden", "FORBIDDEN", ex.getMessage());
    }

    // 兜底
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleAny(Exception ex, WebRequest req) {
        // 这里务必做好日志
        return pd(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "INTERNAL_ERROR", "Unexpected error");
    }
}