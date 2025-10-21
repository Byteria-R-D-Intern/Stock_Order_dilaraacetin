package com.example.stock_order.adapters.web.dto.order;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminOrderSummaryResponse(
        Long id,
        Long userId,
        String status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt
) {}