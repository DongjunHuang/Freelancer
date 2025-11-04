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

    public RefreshToken createAndSaveRefreshToken(String username, String token, Instant expiryDate) {
        RefreshToken entity = new RefreshToken();
        entity.setUsername(username);
        entity.setToken(token);
        entity.setExpireAt(expiryDate);
        return repo.save(entity);
    }

    public boolean validateRefreshToken(String token) {
        return repo.findByToken(token)
            .filter(rt -> rt.getExpireAt().isAfter(Instant.now()))
            .isPresent();
    }

    public Optional<RefreshToken> find(String token) {
        return repo.findByToken(token);
    }

    public boolean isExpired(RefreshToken rt) {
        return rt.getExpireAt().isBefore(Instant.now());
    }

    @Transactional
    public RefreshToken rotate(RefreshToken oldRt, String newToken, Instant newExp) {
        repo.deleteByToken(oldRt.getToken());
        RefreshToken fresh = new RefreshToken();
        fresh.setUsername(oldRt.getUsername());
        fresh.setToken(newToken);
        fresh.setExpireAt(newExp);
        return repo.save(fresh);
    }
}