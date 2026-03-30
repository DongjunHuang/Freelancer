package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@Service
@ConfigurationProperties(prefix = "jwt")
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey key;
    private final JwtParser parser;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(
                this.jwtProperties.getPrivateKey().getBytes(StandardCharsets.UTF_8)
        );
        this.parser = Jwts.parserBuilder()
                                .setSigningKey(this.key)
                                .setAllowedClockSkewSeconds(60)
                                .build();
    }

    /**
     * Generate refresh token.
     * @param username the user name.
     * @param email the email.
     * @return the token generated.
     */
    public String generateRefreshToken(String username, String email) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("email", email);
        return generateToken(map, username, this.jwtProperties.getRefreshExpiration());
    }

    /**
     * Generate access token.
     * @param username the username.
     * @param email the email.
     * @return the token generated.
     */
    public String generateAccessToken(String username, String email) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("email", email);
        return generateToken(map, username, this.jwtProperties.getAccessExpiration());
    }

    /**
     * Generate admin access token.
     * @param username the admin name.
     * @return the token generated.
     */
    public String generateAdminAccessToken(String username) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("role", "ADMIN");
        map.put("type", "ADMIN_ACCESS");
        map.put("iss", "freelancer-admin");
        return generateToken(map, username, this.jwtProperties.getAdminAccessExpiration());
    }

    /**
     * Check if the token is valid.
     * @param jwt token
     * @return whether the token is valid.
     */
    public boolean isValid(String jwt) {
        try {
            parser.parseClaimsJws(jwt);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Parse the token.
     * @param jwt the token
     * @return the parsed info.
     */
    public TokenInfo parse(String jwt) {
        TokenInfo jwtToken = new TokenInfo();
        Jws<Claims> jws = parser.parseClaimsJws(jwt);
        jwtToken.setEmail((String)jws.getBody().get("email"));
        jwtToken.setUsername(jws.getBody().getSubject());
        return jwtToken;
    }

    /**
     * Generate token.
     * 
     * @param extraClaims
     * @param username
     * @param expiration
     * @return
     */
    private String generateToken(Map<String, String> extraClaims, String username, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(this.key)
                .compact();
    }


    private String hmacSha256Base64Url(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(this.jwtProperties.getPrivateKey().getBytes(), "HmacSHA256"));
            byte[] sig = mac.doFinal(message.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generateSignedDeviceId() {
        String deviceId = UUID.randomUUID().toString();

        String sig = hmacSha256Base64Url(deviceId);
        String value = deviceId + "." + sig;
        return value;
    }

    public boolean validateSignedDeviceId(String signedDeviceId) {
        String[] parts = signedDeviceId.split("\\.");
        String id = parts[0];
        String sig = parts[1];

        String expected = hmacSha256Base64Url(id);
        if (!expected.equals(sig)) {
            return false;
        }
        return true;
    }
}