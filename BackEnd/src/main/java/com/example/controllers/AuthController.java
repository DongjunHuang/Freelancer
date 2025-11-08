package com.example.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.requests.SigninReq;
import com.example.requests.SignupReq;
import com.example.security.SecretService;
import com.example.security.JwtUserDetails;
import com.example.security.TokenInfo;
import com.example.services.RefreshTokenService;
import com.example.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


import java.time.Duration;
import java.time.LocalDateTime;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
    private final SecretService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Environment env;
        
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupReq req) {
        userService.signup(req);
        return ResponseEntity.ok(Map.of("message","verification_email_sent"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
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
    public ResponseEntity<?> signin(@RequestBody SigninReq req, HttpServletRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            JwtUserDetails principal = (JwtUserDetails) auth.getPrincipal();        
            String refreshToken = jwtService.generateRefreshToken(principal.getUsername(), principal.getEmail());
            
            String deviceId = jwtService.generateSignedDeviceId();
            String ipAddress = getClientIp(request);
            refreshTokenService.createAndSaveRefreshToken(principal.getId(),
                                                            principal.getUsername(), 
                                                            refreshToken, 
                                                            deviceId,
                                                            ipAddress,
                                                            LocalDateTime.now().plusDays(7));
            
            // Generate cookie sending back to the client.
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(List.of(env.getActiveProfiles()).contains("prod"))        
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
            

            ResponseCookie did = ResponseCookie.from("deviceid", deviceId)
                .httpOnly(true)
                .secure(List.of(env.getActiveProfiles()).contains("prod"))   
                .sameSite("Strict")             
                .path("/")                    
                .maxAge(Duration.ofDays(400)) 
                .build();
            
            var accessToken = jwtService.generateAccessToken(principal.getUsername(), principal.getEmail());
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString(), did.toString())
                .body(Map.of(
                    "accessToken", accessToken,
                    "user", Map.of(
                        "username", principal.getUsername(),
                        "email",    principal.getEmail()
                    )
                ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

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

    @PostMapping("/signout")
    public ResponseEntity<Void> signout(
            @AuthenticationPrincipal JwtUserDetails principal,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @CookieValue(value = "deviceid", required = false) String deviceId) {
        
        // Remove the refresh token in backend database
        refreshTokenService.revokeByUserAndDevice(principal.getId(), deviceId);

        // clear refresh token.
        ResponseCookie clearRt = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(List.of(env.getActiveProfiles()).contains("prod"))
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRt.toString())
                .build();
    }

    @PostMapping("/resendEmail")
    public ResponseEntity<?> resend(@RequestBody Map<String, String> body) {
        String email = body.get("email");        
        userService.resendEmail(email);
        return ResponseEntity.ok(Map.of("message", "If the account exists and is not verified, a new link has been sent."));
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) 
            return ip;
        return request.getRemoteAddr();
    }
}
