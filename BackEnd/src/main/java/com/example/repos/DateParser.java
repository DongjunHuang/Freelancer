package com.example.repos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DateParser {
    private static final List<DateTimeFormatter> DEFAULT_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE, // yyyy-MM-dd
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"), // 04/15/2025
            DateTimeFormatter.ofPattern("M/d/yyyy"), // 4/15/2025
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"), // 5/4/2025
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    public LocalDate parseRecordTime(String raw, String userPattern) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String s = raw.trim();

        List<DateTimeFormatter> formatters = new ArrayList<>();
        if (userPattern != null && !userPattern.isBlank()) {
            formatters.add(DateTimeFormatter.ofPattern(userPattern));
        }
        formatters.addAll(DEFAULT_FORMATTERS);

        for (DateTimeFormatter fmt : formatters) {
            try {
                if (fmt.toString().contains("H")) {
                    return LocalDateTime.parse(s, fmt).toLocalDate();
                }

                return LocalDate.parse(s, fmt);

            } catch (Exception ignored) {
            }
        }

        // 3) Excel serial date
        if (s.matches("\\d+")) {
            long serial = Long.parseLong(s);
            return LocalDate.of(1899, 12, 30).plusDays(serial);
        }

        throw new IllegalArgumentException("Unrecognized date format: " + raw);
    }
}
