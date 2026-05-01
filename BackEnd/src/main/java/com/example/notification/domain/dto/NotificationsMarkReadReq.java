package com.example.notification.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationsMarkReadReq {
    private List<Long> ids;
}