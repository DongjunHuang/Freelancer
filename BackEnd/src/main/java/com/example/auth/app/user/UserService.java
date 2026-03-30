package com.example.auth.app.user;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.auth.domain.user.*;
import com.example.auth.infra.jpa.RefreshTokenRepo;
import com.example.exception.*;
import com.example.exception.types.*;
import com.example.security.JwtService;
import com.example.security.TokenInfo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth.infra.jpa.MailTokenRepo;
import com.example.auth.infra.jpa.UserRepo;
import com.example.utils.TokenUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String DEFAULT_ROLE = "USER";
    private static final int TTL_Hours = 1;
    private static final int REFRESH_TOKEN_TTL_IN_DAYS = 7;

    // The user repo in charge of user database
    private final UserRepo userRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final MailTokenRepo mailTokenRepo;

    private final JwtService jwtService;

    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;
    private final Environment env;

    @Transactional
    public void signup(SignupReq req) {
        userRepo.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new ConflictException(ErrorCode.EMAIL_USED);
        });
        userRepo.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new ConflictException(ErrorCode.USERNAME_USED);
        });

        // Pending waiting for user to verify the email
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .status(UserStatus.PENDING)
                .publicId(UUID.randomUUID().toString())
                .roles(DEFAULT_ROLE)
                .build();
        userRepo.save(user);
        String tokenString = TokenUtils.getRefreshToken();

        MailToken token = mailTokenRepo.findByUserId(user.getUserId())
                .map(existing -> {
                    existing.setToken(tokenString);
                    existing.setExpiresAt(LocalDateTime.now().plusHours(TTL_Hours));
                    existing.setUsed(false);
                    existing.setCreatedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> MailToken.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .token(tokenString)
                        .expiresAt(LocalDateTime.now().plusHours(TTL_Hours))
                        .used(false)
                        .build());
        mailTokenRepo.save(token);

        // Publish the event for email verification
        publisher.publishEvent(new VerificationCreatedEvent(user.getUserId(), user.getEmail(), token.getToken()));
    }

    @Transactional
    public void validateEmail(String token) {
        // TODO: should change to specific exception.
        MailToken tok = mailTokenRepo.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.TOKEN_INVALID));

        if (tok.isUsed() || tok.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepo.findById(tok.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        tok.setUsed(true);
        mailTokenRepo.save(tok);
    }

    @Transactional
    public void resendEmail(String username) {
        // step1: Remove the old one first
        mailTokenRepo.deleteByUsername(username);

        // step2: check if the user is pending
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.USER_IS_NOT_PENDING);
        }

        // step3: issue and save the new generated one
        String tokenString = TokenUtils.getRefreshToken();
        MailToken token = mailTokenRepo.findByUserId(user.getUserId())
                .map(existing -> {
                    existing.setToken(tokenString);
                    existing.setExpiresAt(LocalDateTime.now().plusHours(TTL_Hours));
                    existing.setUsed(false);
                    existing.setCreatedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> MailToken.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .token(tokenString)
                        .expiresAt(LocalDateTime.now().plusHours(TTL_Hours))
                        .used(false)
                        .build());
        mailTokenRepo.save(token);

        // step4: resend the email
        publisher.publishEvent(new VerificationCreatedEvent(user.getUserId(), user.getEmail(), token.getToken()));
    }

    /**
     * Create and save the refresh token. The token genereted will be issue with the
     * email to the user.
     * TODO: we might need to change to enter the code.
     *
     * @param username   the user name.
     * @param token      the token.
     * @param deviceId   device ID generated per device issued by backend.
     * @param ipAddress  the ip address.
     * @param expiryDate the expiry date.
     * @return the token generated.
     */
    @Transactional
    public RefreshToken createAndSaveRefreshToken(String username,
                                                  String token,
                                                  String deviceId,
                                                  String ipAddress,
                                                  LocalDateTime expiryDate) {
        refreshTokenRepo.deleteByUsernameAndDeviceId(username, deviceId);

        RefreshToken entity = RefreshToken.builder()
                .username(username)
                .token(token)
                .expiresAt(expiryDate)
                .ipAddress(ipAddress)
                .deviceId(deviceId)
                .build();
        return refreshTokenRepo.save(entity);
    }


    /**
     * Find the token information from the token string.
     *
     * @param token the token information.
     * @return the token.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    /**
     * Validate the refresh token.
     *
     * @param token the token string.
     * @return whether the token is valid.
     */
    public boolean validateRefreshToken(String token) {
        return refreshTokenRepo.findByToken(token)
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    /**
     * Revoke the token by user name and device id.
     *
     * @param username the usernme.
     * @param deviceId the device id.
     */
    public void revokeByUsernameAndDeviceId(String username, String deviceId) {
        refreshTokenRepo.deleteByUsernameAndDeviceId(username, deviceId);
    }

    /**
     * Check if the token is expired.
     *
     * @param rt the token.
     * @return if expired.
     */
    public boolean isExpired(RefreshToken rt) {
        return rt.getExpiresAt().isBefore(LocalDateTime.now());
    }


    /**
     * Sign in function, mainly to issue refresh token to users.
     *
     * @param username  the username.
     * @param email     the email.
     * @param ipAddress the ipaddress.
     * @return the sign in response.
     */
    public ResponseEntity<SigninResp> signin(String username, String email, String ipAddress) {
        String refreshToken = jwtService.generateRefreshToken(username, email);
        String deviceId = jwtService.generateSignedDeviceId();

        // Create and save refresh token
        createAndSaveRefreshToken(username,
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

        ResponseCookie did = ResponseCookie.from("deviceId", deviceId)
                .httpOnly(true)
                .secure(isProd)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(400))
                .build();

        var accessToken = jwtService.generateAccessToken(username, email);
        SigninResp resp = SigninResp.builder()
                .accessToken(accessToken)
                .user(
                        SigninResp.UserInfo.builder()
                                .username(username)
                                .email(email)
                                .build()
                )
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString(), did.toString())
                .body(resp);
    }

    public RefreshResp refreshAccessToken(String refreshToken, String deviceId) {
        if (refreshToken == null || refreshToken.isBlank() || deviceId == null) {
            throw new BadRequestException(ErrorCode.NOT_VALID_REFRESH_TOKEN);
        }

        if (!jwtService.isValid(refreshToken)) {
            throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
        }

        TokenInfo info = jwtService.parse(refreshToken);
        Optional<RefreshToken> rtoken = refreshTokenRepo.findByToken(refreshToken);

        if (rtoken.isEmpty()) {
            throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
        }

        var rt = rtoken.get();

        if (rt.getExpiresAt().isBefore(LocalDateTime.now())
                || !rt.getDeviceId().equals(deviceId)) {
            throw new AuthenticationException(ErrorCode.TOKEN_EXPIRED);
        }

        String newAccessToken = jwtService.generateAccessToken(
                info.getUsername(),
                info.getEmail()
        );

        return new RefreshResp(newAccessToken);
    }
}