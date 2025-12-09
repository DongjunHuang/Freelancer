package com.example.services;

import com.example.repos.RefreshToken;
import com.example.repos.RefreshTokenRepo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTests {

    @Mock
    private RefreshTokenRepo repo;

    @InjectMocks
    private RefreshTokenService service;

    @Test
    void createAndSaveRefreshToken_shouldDeleteOldAndSaveNew() {
        // given
        String username = "john";
        String token = "refresh-token-123";
        String deviceId = "device-1";
        String ip = "127.0.0.1";
        LocalDateTime expiry = LocalDateTime.now().plusDays(7);

        RefreshToken saved = RefreshToken.builder()
                .id(1L)
                .username(username)
                .token(token)
                .deviceId(deviceId)
                .ipAddress(ip)
                .expiresAt(expiry)
                .build();

        when(repo.save(any(RefreshToken.class))).thenReturn(saved);

        // when
        RefreshToken result = service.createAndSaveRefreshToken(
                username, token, deviceId, ip, expiry);

        // then
        verify(repo, times(1)).deleteByUsernameAndDeviceId(username, deviceId);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repo, times(1)).save(captor.capture());

        RefreshToken toSave = captor.getValue();
        assertThat(toSave.getUsername()).isEqualTo(username);
        assertThat(toSave.getToken()).isEqualTo(token);
        assertThat(toSave.getDeviceId()).isEqualTo(deviceId);
        assertThat(toSave.getIpAddress()).isEqualTo(ip);
        assertThat(toSave.getExpiresAt()).isEqualTo(expiry);

        assertThat(result).isSameAs(saved);
    }

    @Test
    void findByToken_shouldDelegateToRepo() {
        // given
        String token = "abc";
        RefreshToken rt = RefreshToken.builder().token(token).build();

        when(repo.findByToken(token)).thenReturn(Optional.of(rt));

        // when
        Optional<RefreshToken> result = service.findByToken(token);

        // then
        verify(repo, times(1)).findByToken(token);
        assertThat(result).contains(rt);
    }

    @Test
    void validateRefreshToken_shouldReturnTrueWhenTokenExistsAndNotExpired() {
        // given
        String token = "valid-token";
        RefreshToken rt = RefreshToken.builder().token(token).expiresAt(LocalDateTime.now().plusMinutes(10)).build();

        when(repo.findByToken(token)).thenReturn(Optional.of(rt));

        // when
        boolean valid = service.validateRefreshToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void validateRefreshToken_shouldReturnFalseWhenTokenNotFound() {
        // given
        String token = "missing";

        when(repo.findByToken(token)).thenReturn(Optional.empty());

        // when
        boolean valid = service.validateRefreshToken(token);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void validateRefreshToken_shouldReturnFalseWhenTokenExpired() {
        // given
        String token = "expired";
        RefreshToken rt = RefreshToken.builder().token(token).expiresAt(LocalDateTime.now().minusMinutes(1)).build();

        when(repo.findByToken(token)).thenReturn(Optional.of(rt));

        // when
        boolean valid = service.validateRefreshToken(token);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void revokeByUsernameAndDeviceId_shouldDelegateToRepo() {
        // given
        String username = "john";
        String deviceId = "dev-1";

        // when
        service.revokeByUsernameAndDeviceId(username, deviceId);

        // then
        verify(repo, times(1)).deleteByUsernameAndDeviceId(username, deviceId);
    }

    @Test
    void isExpired_shouldReturnTrueWhenExpiryBeforeNow() {
        // given
        RefreshToken rt = RefreshToken.builder().expiresAt(LocalDateTime.now().minusHours(1)).build();

        // when
        boolean expired = service.isExpired(rt);

        // then
        assertThat(expired).isTrue();
    }

    @Test
    void isExpired_shouldReturnFalseWhenExpiryAfterNow() {
        // given
        RefreshToken rt = RefreshToken.builder().expiresAt(LocalDateTime.now().plusHours(1)).build();

        // when
        boolean expired = service.isExpired(rt);

        // then
        assertThat(expired).isFalse();
    }
}