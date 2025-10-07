package com.example.stock_order.adapters.web.dto.cart;

import java.util.List;

public record CartResponse(
    Long userId,
    List<CartItemResponse> items
) {}
