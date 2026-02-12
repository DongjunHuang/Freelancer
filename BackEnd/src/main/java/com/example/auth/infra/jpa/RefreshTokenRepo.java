package com.example.auth.infra.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth.domain.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUsernameAndDeviceId(String username, String deviceId);
}