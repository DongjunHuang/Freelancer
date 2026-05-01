package com.example.notification.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationMarkHeadReq {
    private List<Long> ids;
}