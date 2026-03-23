package com.example.issue.domain;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class MessageCursor {
    private Instant lastMessageAt;
    private Long id;

    public static String encode(ObjectMapper om, Cursor c) {
        try {
            String json = om.writeValueAsString(c);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor encode", e);
        }
    }
}
