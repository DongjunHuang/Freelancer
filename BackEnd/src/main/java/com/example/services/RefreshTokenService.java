package com.example.services;

import java.time.Instant;
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

    public RefreshToken createAndSaveRefreshToken(Long userId, String token, Instant expiryDate) {
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setToken(token);
        entity.setExpiryDate(expiryDate);
        return repo.save(entity);
    }

    public boolean validateRefreshToken(String token) {
        return repo.findByToken(token)
            .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
            .isPresent();
    }

    public Optional<RefreshToken> find(String token) {
        return repo.findByToken(token);
    }

    public boolean isExpired(RefreshToken rt) {
        return rt.getExpiryDate().isBefore(Instant.now());
    }

    public void revokeRefreshToken(Long userId) {
        repo.deleteByUserId(userId);
    }

    @Transactional
    public RefreshToken rotate(RefreshToken oldRt, String newToken, Instant newExp) {
        repo.deleteByToken(oldRt.getToken());
        RefreshToken fresh = new RefreshToken();
        fresh.setUserId(oldRt.getUserId());
        fresh.setToken(newToken);
        fresh.setExpiryDate(newExp);
        return repo.save(fresh);
    }

    public void revokeByUser(Long userId) {
        repo.deleteByUserId(userId);
    }
}