package com.example.repos;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;


@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_device", columnNames = {"user_id", "device_id"})
    }
)
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String token;

    @Column(name = "device_id", nullable = false, length = 128)
    private String deviceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 过期时间 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 吊销标记 */
    @Column(nullable = false)
    private boolean revoked = false;
}