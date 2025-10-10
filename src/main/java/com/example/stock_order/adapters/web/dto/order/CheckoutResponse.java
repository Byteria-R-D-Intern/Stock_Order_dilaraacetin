package com.example.stock_order.adapters.web.dto.order;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutResponse(
    Long orderId,
    String status,
    BigDecimal totalAmount,
    List<Item> items
){
    public record Item(Long productId, String sku, String name, BigDecimal unitPrice, Long quantity, BigDecimal lineTotal){}
}