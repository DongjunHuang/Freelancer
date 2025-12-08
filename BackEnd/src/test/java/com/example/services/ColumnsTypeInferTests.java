package com.example.services;

import org.junit.jupiter.api.Test;

import com.example.repos.ColumnType;
import com.example.utils.ColumnsTypeInfer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColumnsTypeInferTests {
    @Test
    void testInferColumnTypeEmpty() {
        ColumnType result = ColumnsTypeInfer.inferColumnType("anything", Collections.emptyList());
        assertEquals(ColumnType.STRING, result);
    }

    @Test
    void testInferColumnTypeDate() {
        List<String> samples = Arrays.asList(
                "2024-01-01",
                "2024-02-02",
                "not a date"); // 2/3 = 0.666... >= 0.6

        ColumnType result = ColumnsTypeInfer.inferColumnType("trade_date", samples);

        assertEquals(ColumnType.DATE, result);
    }

    @Test
    void testInferColumnTypeNumbers() {
        List<String> samples = Arrays.asList(
                "123",
                "1,234.56",
                "-99.5",
                "12.3%",
                "0.0",
                "100"); // 6/6 numbers => 1.0 >= 0.9

        ColumnType result = ColumnsTypeInfer.inferColumnType("price", samples);

        assertEquals(ColumnType.NUMBER, result);
    }

    @Test
    void testInferColumnTypeStrings() {
        List<String> samples = Arrays.asList(
                "1001",
                "1002",
                "1003",
                "1004",
                "1005");

        ColumnType result = ColumnsTypeInfer.inferColumnType("user_id", samples);

        assertEquals(ColumnType.STRING, result);
    }

    @Test
    void testInferColumnTypeNoInferredTypes() {
        // Fall back string
        List<String> samples = Arrays.asList(
                "123", // number
                "abc", // string
                "2024-01-01" // date
        );

        ColumnType result = ColumnsTypeInfer.inferColumnType("mixed", samples);

        assertEquals(ColumnType.STRING, result);
    }

    // ---------- looksLikeNumber ----------

    @Test
    void testLooksLikeNumberValid() {
        assertTrue(ColumnsTypeInfer.looksLikeNumber("123"));
        assertTrue(ColumnsTypeInfer.looksLikeNumber("123.45"));
        assertTrue(ColumnsTypeInfer.looksLikeNumber("1,234.56"));
        assertTrue(ColumnsTypeInfer.looksLikeNumber("12.3%"));
        assertTrue(ColumnsTypeInfer.looksLikeNumber("-99.5"));
    }

    @Test
    void testLooksLikeNumberInvalid() {
        assertFalse(ColumnsTypeInfer.looksLikeNumber("abc"));
        assertFalse(ColumnsTypeInfer.looksLikeNumber("2024-01-01"));
        assertFalse(ColumnsTypeInfer.looksLikeNumber(""));
        assertFalse(ColumnsTypeInfer.looksLikeNumber("   "));
        // TODO: add the test case for this specific corner case
        // assertFalse(ColumnsTypeInfer.looksLikeNumber("1,23,4"));
    }

    // ---------- looksLikeDate ----------

    @Test
    void testLooksLikeDateValid() {
        assertTrue(ColumnsTypeInfer.looksLikeDate("2024-01-01"));
        assertTrue(ColumnsTypeInfer.looksLikeDate("2024/12/31"));
    }

    @Test
    void looksLikeDate_yyyyMMddSlash_true() {
        assertTrue(ColumnsTypeInfer.looksLikeDate("2024/12/31"));
    }

    @Test
    void testLooksLikeDateInValid() {
        assertFalse(ColumnsTypeInfer.looksLikeDate("01-01-2024"));
        assertFalse(ColumnsTypeInfer.looksLikeDate("2024.01.01"));
        assertFalse(ColumnsTypeInfer.looksLikeDate("not-a-date"));
        assertFalse(ColumnsTypeInfer.looksLikeDate(""));
    }

    @Test
    void testLooksLikeDateDateThreshold() {
        List<String> samples = Arrays.asList(
                "2024-01-01",
                "2024-02-02",
                "2024/03/03",
                "2024/04/04",
                "not-date",
                "123");

        ColumnType result = ColumnsTypeInfer.inferColumnType("some_date_col", samples);

        assertEquals(ColumnType.DATE, result);
    }

    @Test
    void testLooksLikeNumberThresholdForNumberIsRespected() {
        List<String> samples = Arrays.asList(
                "1", "2", "3", "4", "5", "6", "7", "not-number");

        ColumnType result = ColumnsTypeInfer.inferColumnType("amount", samples);

        assertEquals(ColumnType.STRING, result);
    }
}