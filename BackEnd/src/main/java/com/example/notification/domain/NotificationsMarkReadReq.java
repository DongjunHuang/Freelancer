package com.example.notification.domain;

import lombok.Data;

import java.util.List;

@Data
public class NotificationsMarkReadReq {
    private List<Long> ids;
}