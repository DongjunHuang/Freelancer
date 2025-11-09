package com.example.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.repos.RefreshToken;
import com.example.repos.RefreshTokenRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepo repo;

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

    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    public boolean validateRefreshToken(String token) {
        return repo.findByToken(token)
            .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()))
            .isPresent();
    }
    
    @Transactional
    public void revokeByUsernameAndDeviceId(String username, String deviceId) {
        repo.deleteByUsernameAndDeviceId(username, deviceId);
    }

    public boolean isExpired(RefreshToken rt) {
        return rt.getExpiresAt().isBefore(LocalDateTime.now());
    }
}