package com.example.notification.infra.jpa;

import com.example.notification.domain.NotificationCategory;

public interface NotificationCategoryCountProjection {

    NotificationCategory getCategory();

    long getUnreadCount();
}