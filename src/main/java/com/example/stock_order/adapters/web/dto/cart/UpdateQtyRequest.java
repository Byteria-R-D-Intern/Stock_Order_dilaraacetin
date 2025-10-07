package com.example.stock_order.adapters.web.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateQtyRequest(
    @NotNull Long productId,
    @PositiveOrZero Long quantity
) {}
