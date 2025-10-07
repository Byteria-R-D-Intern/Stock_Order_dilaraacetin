package com.example.stock_order.adapters.web.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
    Long productId,
    String sku,
    String name,
    BigDecimal unitPrice,
    Long quantity
) {}
