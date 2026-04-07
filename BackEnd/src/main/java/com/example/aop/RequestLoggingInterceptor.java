package com.example.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = "anonymous";

        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        log.info("Request start user={}, method={}, uri={}",
                username,
                request.getMethod(),
                request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        log.info("Request end status={}, uri={}",
                response.getStatus(),
                request.getRequestURI());
    }
}