package com.example.stock_order.adapters.web.dto.product;

import com.example.stock_order.domain.model.ProductStock;

public record StockResponse(Long productId, Long quantityOnHand) {
    public static StockResponse from(ProductStock s) {
        return new StockResponse(s.getProductId(), s.getQuantityOnHand());
    }
}