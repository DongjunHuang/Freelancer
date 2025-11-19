package com.example.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.repos.ColumnType;

public class ColumnsTypeInfer {
    private static final DateTimeFormatter[] DATE_PATTERNS = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ISO_LOCAL_DATE
        // TODO: add more
    };

    // infer the column type
    public static ColumnType inferColumnType(String columnName, List<String> samples) {
        if (samples.isEmpty()) {
            return ColumnType.STRING;
        }

        int dateCount = 0;
        int numberCount = 0;
        int total = samples.size();

        for (String v : samples) {
            if (looksLikeDate(v)) {
                dateCount++;
            }
            if (looksLikeNumber(v)) {
                numberCount++;
            }
        }

        if (dateCount >= total * 0.6) { 
            return ColumnType.DATE;
        }

        if (numberCount >= total * 0.9) {
            String lower = columnName.toLowerCase();
            if (lower.contains("id")) {
                return ColumnType.STRING;
            }
            return ColumnType.NUMBER;
        }

        return ColumnType.STRING;
    }

    public static boolean looksLikeNumber(String value) {
        try {
            String cleaned = value
                .replace(",", "")   // 1,234.56
                .replace("%", "");  // 12.3%
            Double.parseDouble(cleaned);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean looksLikeDate(String value) {
        for (DateTimeFormatter fmt : DATE_PATTERNS) {
            try {
                LocalDate.parse(value, fmt);
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }
}
