package com.example.notification.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCursor {
    private Instant createdAt;
    private Long id;

    public static String encode(NotificationCursor cursor, ObjectMapper mapper) {
        try {
            String json = mapper.writeValueAsString(cursor);
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode notification cursor", e);
        }
    }

    public static NotificationCursor decode(String cursor, ObjectMapper mapper) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            return mapper.readValue(json, NotificationCursor.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid notification cursor", e);
        }
    }
}