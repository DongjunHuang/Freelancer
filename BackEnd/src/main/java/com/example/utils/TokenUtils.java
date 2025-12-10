package com.example.utils;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

public class TokenUtils {
    /**
     * The util function to retrieve the refresh token.
     * 
     * @return the refresh token string.
     */
    public static String getRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphanumeric(32);
    }
}
