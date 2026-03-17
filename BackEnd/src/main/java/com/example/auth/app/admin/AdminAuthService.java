package com.example.auth.app.admin;

import com.example.auth.domain.admin.AdminProfileDto;
import com.example.auth.domain.admin.AdminSigninReq;
import com.example.auth.domain.admin.AdminSigninResp;
import com.example.auth.domain.user.User;
import com.example.auth.infra.jpa.UserRepo;
import com.example.exception.AuthenticationException;
import com.example.exception.ErrorCode;
import com.example.security.JwtUserDetails;
import com.example.security.SecretService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final SecretService jwtService;

    public AdminSigninResp signin(AdminSigninReq req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        JwtUserDetails principal = (JwtUserDetails) authentication.getPrincipal();

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new AuthenticationException(ErrorCode.NOT_VALID_ADMIN);
        }

        // 生成 admin token
        String token = jwtService.generateAdminAccessToken(principal.getUsername());

        return AdminSigninResp.builder()
                .accessToken(token)
                .build();
    }
}
