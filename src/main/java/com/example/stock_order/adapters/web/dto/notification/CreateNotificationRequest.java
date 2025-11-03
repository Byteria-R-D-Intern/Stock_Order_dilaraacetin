package com.example.stock_order.adapters.web.dto.notification;

import com.example.stock_order.domain.model.Notification;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull Long userId,
        @NotNull Notification type,
        @Size(max = 255) String title,
        String message
) {}
