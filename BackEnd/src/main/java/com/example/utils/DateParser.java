package com.example.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DateParser {
    private static final List<DateTimeFormatter> DEFAULT_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
    );

    public static Instant parseRecordTime(String raw, String userPattern, String timezone) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        ZoneId zone = resolveZone(timezone);
        String s = raw.trim();

        List<DateTimeFormatter> formatters = new ArrayList<>();

        if (userPattern != null && !userPattern.isBlank()) {
            formatters.add(DateTimeFormatter.ofPattern(userPattern));
        }

        formatters.addAll(DEFAULT_FORMATTERS);

        for (DateTimeFormatter formatter : formatters) {
            Instant instant = tryParse(s, formatter, zone);
            if (instant != null) {
                return instant;
            }
        }

        if (s.matches("\\d+")) {
            long serial = Long.parseLong(s);
            LocalDate date = LocalDate.of(1899, 12, 30).plusDays(serial);
            return date.atStartOfDay(zone).toInstant();
        }

        throw new IllegalArgumentException("Unrecognized date format: " + raw);
    }

    private static Instant tryParse(String raw, DateTimeFormatter formatter, ZoneId zone) {
        try {
            return OffsetDateTime.parse(raw, formatter).toInstant();
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(raw, formatter).atZone(zone).toInstant();
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(raw, formatter).atStartOfDay(zone).toInstant();
        } catch (Exception ignored) {
        }

        return null;
    }

    private static ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }

        try {
            return ZoneId.of(timezone.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone, e);
        }
    }
}