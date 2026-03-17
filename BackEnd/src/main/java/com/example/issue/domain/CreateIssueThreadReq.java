package com.example.issue.domain;

import lombok.*;

@Data
public class CreateIssueThreadReq {
    private String title;
    private String description;
    private String impact; // Optional
    private IssueType type;
}