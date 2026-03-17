package com.example.auth.interfaces;

import com.example.auth.app.admin.AdminAuthService;
import com.example.auth.domain.admin.AdminSigninReq;
import com.example.auth.domain.admin.AdminSigninResp;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);

    private final AdminAuthService adminAuthService;

    @PostMapping("/signin")
    public ResponseEntity<AdminSigninResp> signin(@RequestBody AdminSigninReq req) {
        logger.info("Login for admin username {}", req.getUsername());
        AdminSigninResp resp = adminAuthService.signin(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshCookie,
            @CookieValue(value = "deviceid", required = false) String deviceId) {
        return null;
    }
}
