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

        // 1. 尝试所有格式
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                // 如果格式中包含时间部分，用 LocalDateTime 解析后转 LocalDate
                if (fmt == DateTimeFormatter.ISO_LOCAL_DATE_TIME ||
                    fmt == DateTimeFormatter.ISO_OFFSET_DATE_TIME ||
                    fmt.toString().contains("HH")) {

                    return LocalDateTime.parse(s, fmt).toLocalDate();
                }

                return LocalDate.parse(s, fmt);

            } catch (Exception ignored) { }
        }

        // 2. Excel 序列号（比如 44803 → 2022-09-01）
        if (s.matches("\\d+")) {
            long serial = Long.parseLong(s);
            // Excel 从 1899-12-30 开始算
            return LocalDate.of(1899, 12, 30).plusDays(serial);
        }

        // 3. 全都解析不了（开发模式可以抛错，生产建议记录日志）
        throw new IllegalArgumentException("Unrecognized date format: " + raw);
    }
}
