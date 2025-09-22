package com.example.stock_order.adapters.web.dto.product;

import java.math.BigDecimal;

import com.example.stock_order.domain.model.Product;

public record CreateProductRequest(
        String sku,
        String name,
        String description,
        BigDecimal price,
        Product.Status status,
        Long initialQuantity
) {}