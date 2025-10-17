package com.example.stock_order.adapters.web.dto.product;

import java.math.BigDecimal;

import com.example.stock_order.domain.model.Product;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String status,
        Long quantityOnHand
) {
    public static ProductResponse of(Product p, Long qoh) {
        return new ProductResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getCurrentPrice(),
                p.getStatus() != null ? p.getStatus().name() : null,
                qoh
        );
    }

    public static ProductResponse of(Product p) {
        return of(p, null);
    }
}