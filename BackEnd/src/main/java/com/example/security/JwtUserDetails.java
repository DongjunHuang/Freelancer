package com.example.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.repos.User;
import com.example.repos.UserStatus;

import lombok.Data;

@Data
public class JwtUserDetails implements UserDetails {
    private final Long id;
    private final String publicId;
    private final String username;
    private final String email;
    private final UserStatus uStatus;
    
    public JwtUserDetails(User user) {
        this.id = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.uStatus = user.getStatus();
        this.publicId = user.getPublicId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        // Keep empty, we do not want to expose password.
        throw new UnsupportedOperationException("Unimplemented method 'getPassword'");
    }
}