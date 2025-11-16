package com.example.security;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.repos.User;

import lombok.Data;

@Data
public class JwtUserDetails implements UserDetails {
    private User user;
    Collection<GrantedAuthority> authorities;

    public JwtUserDetails(User user, Collection<GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
       return user.getUsername();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public Long getId() {
        return user.getUserId();
    }
}