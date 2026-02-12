package com.example.feedback.domain;

import lombok.*;

@Data
public class CreateThreadReq {
    private String title;
    private String message;
}