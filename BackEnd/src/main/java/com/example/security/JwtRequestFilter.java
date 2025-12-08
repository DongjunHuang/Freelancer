package com.example.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * The JWT request filter, this class mainly authorize the user to do the
 * certain operations.
 */
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private final SecretService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER)) {
            chain.doFilter(req, res);
            return;
        }

        // Verify the token, if not valid, do not fill the security context let security
        // config handles invalid token.
        String token = header.substring(BEARER.length()).trim();

        if (!jwtService.isValid(token)) {
            chain.doFilter(req, res);
            return;
        }

        // Fill the context to pass the later security check
        TokenInfo accessToken = jwtService.parse(token);
        if (accessToken.getUsername() != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails ud = userDetailsService.loadUserByUsername(accessToken.getUsername());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, null,
                    ud.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
        }

        chain.doFilter(req, res);
    }
}