package com.example.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.repos.UserStatus;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Auth controller handles user related activities, including sign in, sign up,
 * sign out, etc.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final static int REFRESH_TOKEN_TTL_IN_DAYS = 7;
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

    /**
     * User sign up.
     * 
     * @param req the sign up request.
     * @return the response to user sign up.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupReq req) {
        userService.signup(req);
        return ResponseEntity.ok(Map.of("message", "verification_email_sent"));
    }

    /**
     * Validate email.
     * 
     * @param token the token sent by user.
     * @return whether validate email successfully.
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        userService.validateEmail(token);
        return ResponseEntity.ok(Map.of("message", "verified"));
    }

    /**
     * Signin and issue accesstoken and refresh token to the client. At the first
     * phase, only consider website.
     * 
     * @param req the sign in request.
     * @return
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninReq req, HttpServletRequest request) {
        try {
            logger.info("Signin for user {}", req.getUsername());
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            JwtUserDetails principal = (JwtUserDetails) auth.getPrincipal();
            if (principal.getStatus() == UserStatus.PENDING) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "EMAIL_NOT_VERIFIED",
                                "message", "The account not validated"));
            }

            String refreshToken = jwtService.generateRefreshToken(principal.getUsername(), principal.getEmail());
            String deviceId = jwtService.generateSignedDeviceId();
            String ipAddress = getClientIp(request);

            // Create and save refresh token
            refreshTokenService.createAndSaveRefreshToken(principal.getUsername(),
                    refreshToken,
                    deviceId,
                    ipAddress,
                    LocalDateTime.now().plusDays(REFRESH_TOKEN_TTL_IN_DAYS));

            boolean isProd = List.of(env.getActiveProfiles()).contains("prod");

            // Generate cookie sending back to the client.
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(isProd)
                    .path("/auth")
                    .maxAge(Duration.ofDays(REFRESH_TOKEN_TTL_IN_DAYS))
                    .sameSite("Strict")
                    .build();

            ResponseCookie did = ResponseCookie.from("deviceid", deviceId)
                    .httpOnly(true)
                    .secure(isProd)
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
                                    "email", principal.getEmail())));
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
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshCookie,
            @CookieValue(value = "deviceid", required = false) String deviceId) {
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String access = authHeader.substring(7);
            if (jwtService.isValid(access)) {
                username = jwtService.parse(access).getUsername();
            }
        }

        if (username == null && refreshCookie != null && deviceId != null) {
            if (jwtService.isValid(refreshCookie)) {
                var rt = refreshTokenService.findByToken(refreshCookie).orElse(null);
                if (rt != null && rt.getExpiresAt().isAfter(LocalDateTime.now())) {
                    username = rt.getUsername();
                }
            }
        }

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logger.info("Username is " + username + "device Id is " + deviceId);
        refreshTokenService.revokeByUsernameAndDeviceId(username, deviceId);

        ResponseCookie clearRT = ResponseCookie.from("refreshToken", "")
                .path("/auth")
                .httpOnly(true)
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRT.toString())
                .build();
    }

    @PostMapping("/resendEmail")
    public ResponseEntity<?> resend(@RequestBody Map<String, String> body) {
        String username = body.get("username");

        try {
            userService.resendEmail(username);
        } catch (Exception ex) {
            // TODO: return the exception to the user
        }
        return ResponseEntity
                .ok(Map.of("message", "If the account exists and is not verified, a new link has been sent."));
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
