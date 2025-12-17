package com.example.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.repos.ColumnType;

/**
 * The utility class to infer the type of the columns.
 */
public class ColumnsTypeInfer {
    // The symbols that defined the metric is not metric with headers
    private static final String[] NON_METRIC_COLUMN_SYMBOLS = { "id", "code", "no", "number", "zip", "postal", "date" };

    // The date format pattern
    private static final DateTimeFormatter[] DATE_PATTERNS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ISO_LOCAL_DATE
            // TODO: add more date format to satify more.
    };

    /**
     * To infer the column type according to the samples gathered for corresponding
     * header.
     * 
     * @param columnName the column name.
     * @param samples    the samples gathered.
     * 
     * @return the type infered.
     */
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

        // We give the ratio 60% for date
        if (dateCount >= total * 0.6) {
            return ColumnType.DATE;
        }

        // We give the ratio 90% for number count.
        if (numberCount >= total * 0.9) {
            String lower = columnName.toLowerCase();
            if (lower.contains("id")) {
                return ColumnType.STRING;
            }
            return ColumnType.NUMBER;
        }

        return ColumnType.STRING;
    }

    /**
     * Check if the value looks like number.
     * 
     * @param value the input value.
     * 
     * @return whether the value seems like number or not.
     */
    public static boolean looksLikeNumber(String value) {
        try {
            String cleaned = value
                    .replace(",", "") // 1,234.56 TODO: the , should seperate every 3 digits
                    .replace("%", ""); // 12.3%
            Double.parseDouble(cleaned);
            return true;
        } catch (Exception e) {
            // TODO: Throw specific exceptions.
            return false;
        }
    }

    /**
     * Check if the value looks like date.
     * 
     * @param value the input value.
     * 
     * @return whether the value seems like date.
     */
    public static boolean looksLikeDate(String value) {
        for (DateTimeFormatter fmt : DATE_PATTERNS) {
            try {
                LocalDate.parse(value, fmt);
                return true;
            } catch (Exception ignored) {
                // TODO: handle the exception here
            }
        }
        return false;
    }

    /**
     * Helper function, to check if current number header represents metric columns
     * because we infer
     * some number columns maybe not real numbers, for example
     * for zip code, phone number.
     * 
     * @param header the original header name.
     * @param type   the type of the column.
     * @return whether the column is metric.
     */
    public static boolean isMetricColumn(String header, ColumnType type) {
        // If the type if not number, then should not be metric
        if (type != ColumnType.NUMBER || header == null) {
            return false;
        }

        String h = header.trim().toLowerCase();
        for (String symbol : NON_METRIC_COLUMN_SYMBOLS) {
            if (h.contains(symbol)) {
                return false;
            }
        }

        return true;
    }
}
