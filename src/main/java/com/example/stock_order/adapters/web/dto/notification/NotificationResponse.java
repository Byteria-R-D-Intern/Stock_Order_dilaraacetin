package com.example.stock_order.adapters.web.dto.notification;

import java.time.Instant;

import com.example.stock_order.domain.model.Notification;
import com.example.stock_order.infrastructure.persistence.entity.NotificationEntity;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        Notification type,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse of(NotificationEntity e) {
        return new NotificationResponse(
                e.getId(),
                e.getTitle(),
                e.getMessage(),
                e.getType(),
                e.isRead(),
                e.getCreatedAt()
        );
    }
}
