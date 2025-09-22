package com.example.stock_order.adapters.web.dto.product;


import java.math.BigDecimal;

import com.example.stock_order.domain.model.Product;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        Product.Status status
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getSku(), p.getName(), p.getCurrentPrice(), p.getStatus()
        );
    }
}