package com.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtRequestFilter
 */
@ExtendWith(MockitoExtension.class)
public class JwtRequestFilterTests {

    @Mock
    SecretService jwtService;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @InjectMocks
    JwtRequestFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testWhenNoAuthorizationHeader() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testWhenAuthorizationHeaderNotBearerHeader() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Token abc.def");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testWhenTokenInvalid() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.isValid("invalid-token")).thenReturn(false);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(jwtService).isValid("invalid-token");
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testWhenTokenValidAndNoExistingAuth() throws ServletException, IOException {
        // given
        String token = "good-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);

        TokenInfo info = new TokenInfo();
        info.setUsername("john");
        info.setEmail("john@example.com");
        when(jwtService.parse(token)).thenReturn(info);

        UserDetails ud = new User(
                "john",
                "pw",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userDetailsService.loadUserByUsername("john")).thenReturn(ud);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(jwtService).isValid(token);
        verify(jwtService).parse(token);
        verify(userDetailsService).loadUserByUsername("john");
        verify(filterChain).doFilter(request, response);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getPrincipal()).isEqualTo(ud);
        assertThat(auth.getAuthorities()).containsExactlyElementsOf(ud.getAuthorities());
    }

    @Test
    void testWhenContextAlreadyHasAuthentication() throws ServletException, IOException {
        UserDetails existingUser = new User(
                "existing",
                "pw",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        String token = "good-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);

        TokenInfo info = new TokenInfo();
        info.setUsername("john");
        info.setEmail("john@example.com");
        when(jwtService.parse(token)).thenReturn(info);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(jwtService).isValid(token);
        verify(jwtService).parse(token);
        
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo(existingUser);
    }
}