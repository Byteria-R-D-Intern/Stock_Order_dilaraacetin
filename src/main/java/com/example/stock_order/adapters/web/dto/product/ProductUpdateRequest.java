package com.example.stock_order.adapters.web.dto.product;

import java.math.BigDecimal;

import com.example.stock_order.domain.model.Product;

public record ProductUpdateRequest(
        String name,
        String description,
        BigDecimal price,
        Product.Status status 
) {
    
}
