package com.example.stock_order.adapters.web.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminOrderDetailResponse(
        Long id,
        Long userId,
        String status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt,
        List<Item> items
) {
    public record Item(Long productId, String sku, String name,
                       BigDecimal unitPrice, Long quantity, BigDecimal lineTotal) {}
}