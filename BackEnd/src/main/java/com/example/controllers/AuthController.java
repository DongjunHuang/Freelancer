package com.example.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.requests.SigninReq;
import com.example.requests.SignupReq;
import com.example.security.JwtService;
import com.example.security.JwtUserDetails;
import com.example.security.TokenInfo;
import com.example.services.RefreshTokenService;
import com.example.services.UserService;

import lombok.RequiredArgsConstructor;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * The message service.
     */
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Environment env;
        
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupReq req) {
        userService.signup(req);
        return ResponseEntity.ok(Map.of("message","verification_email_sent"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        logger.info("Start verifying the token: " + token);
        userService.validateEmail(token);
        return ResponseEntity.ok(Map.of("message","verified"));
    }

    /**
     * Signin and issue accesstoken and refresh token to the client. At the first phase, only consider website.
     * 
     * @param req
     * @return
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninReq req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            JwtUserDetails principal = (JwtUserDetails) auth.getPrincipal();
            String accessToken = jwtService.generateAccessToken(principal.getUsername(), principal.getEmail());
            String refreshToken = jwtService.generateRefreshToken(principal.getUsername(), principal.getEmail());
            logger.info("username: " + principal.getUsername() + ", email: " + principal.getEmail());

            Instant instant = LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant();
            refreshTokenService.createAndSaveRefreshToken(principal.getUsername(), refreshToken, instant);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(List.of(env.getActiveProfiles()).contains("prod"))        
                .path("/auth/refresh") // The only api direction should contain the cookie
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
            logger.info("The access token is " + accessToken);    
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                "accessToken", accessToken,
                "userId", principal.getId(),
                "username", principal.getUsername()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    // TODO: implement
    @PostMapping("/signout")
    public ResponseEntity<?> logout() {
        return null;
    }

    //TODO: test
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("refreshToken") String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing refresh token");
        }
    
        if (!jwtService.isValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
        TokenInfo info = jwtService.parse(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(info.getUsername(), info.getEmail());
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
