package com.example.issue.domain.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public class Utils {
    public static String encodeMessageCursor(Instant createdAt, Long id) {
        try {
            String json = new ObjectMapper().writeValueAsString(Map.of(
                    "createdAt", createdAt.toString(),
                    "id", id
            ));
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode cursor", e);
        }
    }
}
