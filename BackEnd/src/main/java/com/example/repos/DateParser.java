package com.example.repos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class DateParser {
    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
        DateTimeFormatter.ISO_LOCAL_DATE,                       // yyyy-MM-dd
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),              // yyyy/MM/dd
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),              // 10/05/2024 (US)
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),              // 05/10/2024 (EU)
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),     // yyyy-MM-dd 12:30:00
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),     // yyyy/MM/dd 08:12:55
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,                  // 2024-10-05T12:30:00
        DateTimeFormatter.ISO_OFFSET_DATE_TIME                  // 2024-10-05T12:30:00Z
    };

    public LocalDate parseRecordTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String s = raw.trim();

        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                
                if (fmt == DateTimeFormatter.ISO_LOCAL_DATE_TIME ||
                    fmt == DateTimeFormatter.ISO_OFFSET_DATE_TIME ||
                    fmt.toString().contains("HH")) {

                    return LocalDateTime.parse(s, fmt).toLocalDate();
                }

                return LocalDate.parse(s, fmt);

            } catch (Exception ignored) { }
        }

        if (s.matches("\\d+")) {
            long serial = Long.parseLong(s);
            return LocalDate.of(1899, 12, 30).plusDays(serial);
        }

        throw new IllegalArgumentException("Unrecognized date format: " + raw);
    }
}
