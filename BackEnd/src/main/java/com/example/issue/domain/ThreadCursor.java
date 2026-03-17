package com.example.issue.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * The helper cursor pointing to the last visited mesages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadCursor {
    private Instant lastMessageAt;
    private Long id;

    public static String encode(ObjectMapper om, ThreadCursor c) {
        try {
            String json = om.writeValueAsString(c);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor encode", e);
        }
    }

    public static ThreadCursor decode(ObjectMapper om, String cursor) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            return om.readValue(new String(bytes, StandardCharsets.UTF_8), ThreadCursor.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor decode", e);
        }
    }
}