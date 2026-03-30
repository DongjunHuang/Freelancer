package com.example.auth.interfaces;

import com.example.auth.domain.user.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.app.user.UserService;
import com.example.exception.ErrorCode;
import com.example.exception.types.AuthenticationException;

import com.example.security.JwtUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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

/**
 * Auth controller handles user related activities, including sign in, sign up,
 * sign out, etc.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserAuthController {
    private static final String DEVICE_ID_COOKIE_KEY = "deviceId";
    private static final String REFRESH_TOKEN_COOKIE_KEY = "refreshToken";

    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserAuthController.class);

    /**
     * The message service.
     */
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    /**
     * User sign up.
     *
     * @param req the signup request.
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
    public ResponseEntity<SigninResp> signin(
            @RequestBody SigninReq req,
            HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        JwtUserDetails principal = (JwtUserDetails) auth.getPrincipal();
        if (principal.getStatus() == UserStatus.PENDING) {
            throw new AuthenticationException(ErrorCode.USER_IS_NOT_VERIFIED);
        }

        boolean isUser = principal.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));

        if (!isUser) {
            throw new AuthenticationException(ErrorCode.USER_NOT_FOUND);
        }

        String ipAddress = getClientIp(request);
        ResponseEntity<SigninResp> resp = userService.signin(principal.getUsername(), principal.getEmail(), ipAddress);
        return resp;
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResp> refreshAccessToken(
            @CookieValue(REFRESH_TOKEN_COOKIE_KEY) String refreshToken,
            @CookieValue(DEVICE_ID_COOKIE_KEY) String deviceId) {
        logger.info("The token is {} and device id is {}", refreshToken, deviceId);
        RefreshResp resp = userService.refreshAccessToken(refreshToken, deviceId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signout(
            @AuthenticationPrincipal JwtUserDetails user,
            @CookieValue(DEVICE_ID_COOKIE_KEY) String deviceId) {
        if (user == null || deviceId == null) {
            throw new AuthenticationException(ErrorCode.USER_IS_NOT_VERIFIED);
        }

        userService.revokeByUsernameAndDeviceId(user.getUsername(), deviceId);

        ResponseCookie clearRT = ResponseCookie.from(REFRESH_TOKEN_COOKIE_KEY, "")
                .path("/auth")
                .httpOnly(true)
                .maxAge(0)
                .build();

        ResponseCookie clearDid = ResponseCookie.from(DEVICE_ID_COOKIE_KEY, "")
                .path("/auth")
                .httpOnly(true)
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRT.toString(), clearDid.toString())
                .build();
    }

    @PostMapping("/resendEmail")
    public ResponseEntity<?> resend(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        userService.resendEmail(username);
        return ResponseEntity
                .ok(Map.of("message", "If the account exists and is not verified, a new link has been sent."));
    }

    /**
     * Get the ip address of the clients.
     *
     * @param request the request
     * @return the ip address.
     */
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
