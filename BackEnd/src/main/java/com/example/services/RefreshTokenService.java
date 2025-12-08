package com.example.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.repos.RefreshToken;
import com.example.repos.RefreshTokenRepo;

import lombok.RequiredArgsConstructor;

/**
 * The refresh token service to issue refresh token to newly created client.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepo repo;

    /**
     * Create and save the refresh token. The token genereted will be issue with the email to the user.
     * TODO: we might need to change to enter the code.
     * 
     * @param username the user name.
     * @param token the token.
     * @param deviceId device ID generated per device issued by backend.
     * @param ipAddress the ip address.
     * @param expiryDate the expiry date.
     * 
     * @return the token generated.
     */
    @Transactional
    public RefreshToken createAndSaveRefreshToken(String username, 
                                            String token, 
                                            String deviceId,
                                            String ipAddress,
                                            LocalDateTime expiryDate) {
        repo.deleteByUsernameAndDeviceId(username, deviceId);
        
        RefreshToken entity = new RefreshToken();
        entity.setUsername(username);
        entity.setToken(token);
        entity.setExpiresAt(expiryDate);
        entity.setIpAddress(ipAddress);
        entity.setDeviceId(deviceId);
        return repo.save(entity);
    }

    /**
     * Find the token information from the token string.
     * 
     * @param token the token information.
     * @return the token.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    /**
     * Validate the refresh token.
     * 
     * @param token the token string.
     * @return whether  the token is valid.
     */
    public boolean validateRefreshToken(String token) {
        return repo.findByToken(token)
            .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()))
            .isPresent();
    }
    
    /**
     * Revoke the token by user name and device id.
     * 
     * @param username the usernme.
     * @param deviceId the device id.
     */
    @Transactional
    public void revokeByUsernameAndDeviceId(String username, String deviceId) {
        repo.deleteByUsernameAndDeviceId(username, deviceId);
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
}