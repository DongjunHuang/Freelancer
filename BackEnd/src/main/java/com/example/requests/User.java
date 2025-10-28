package com.example.requests;

import java.security.Timestamp;

import lombok.Data;

@Data
public class User {
    /**
     * The username.
     */
    private String userName;

    /**
     * The user id.
     */
    private String userId;

    /**
     * The user creation time.
     */
    private Timestamp userCreationTime;

    /**
     * The user email.
     */
    private String userEmail;

    /**
     * The user role, either 0 = ADMIN, 2 = TEST or 1= NORMAL.
     *
     */
    private int userRole;

    /**
     * The user setting: summarize automatically or manually.
     */
    private Boolean userSettingSummarizeAutomatically;

    /**
     * The user setting: manually post message or automatically upload message.
     */
    private Boolean userSettingManualPostMessage;
}
