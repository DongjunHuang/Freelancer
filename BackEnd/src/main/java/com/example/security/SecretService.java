package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.security.Key;
import java.util.Base64;

@Service
public class SecretService {
    private final JwtParser parser;
    
    // Access Token TTL    
    private final long ACCESS_EXPIRATION = 1000 * 60 * 15;

    // Refresh Token TTL
    private final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    // The generated key according to private key string
    private String privateKeyBase64;
    private Key key;
    public SecretService(@Value("${jwt.private-key}") String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(privateKeyBase64));
        this.parser = Jwts.parserBuilder()
                                .setSigningKey(this.key)
                                .setAllowedClockSkewSeconds(60)
                                .build();
    }

    /**
     * Generate refersh token.
     * @param username
     * @return
     */
    public String generateRefreshToken(String username, String email) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("email", email);
        return generateToken(map, username, REFRESH_EXPIRATION);
    }
    
    /**
     * Generate access token.
     * 
     * @param username
     * @return
     */
    public String generateAccessToken(String username, 
                                        String email) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("email", email);
        return generateToken(map, username, ACCESS_EXPIRATION);
    }


    public boolean isValid(String jwt) {
        try {
            parser.parseClaimsJws(jwt);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

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
            mac.init(new SecretKeySpec(this.privateKeyBase64.getBytes(), "HmacSHA256"));
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