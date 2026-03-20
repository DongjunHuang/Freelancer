package com.example.issue.domain.user;

import com.example.issue.domain.common.IssueType;
import lombok.*;

@Data
public class CreateIssueThreadReq {
    private String title;
    private String description;
    private String impact; // Optional
    private IssueType type;
}