package com.example.feedback.domain;

import lombok.*;

@Data
public class CreateFeedbackReq {
    private String title;
    private String message;
}