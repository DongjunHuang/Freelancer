package com.example.auth.domain.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProfileDto {
    private Long id;
    private String email;
    private String name;
}