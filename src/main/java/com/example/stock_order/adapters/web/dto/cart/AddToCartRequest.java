package com.example.stock_order.adapters.web.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddToCartRequest(
    @NotNull Long productId,
    @Positive Long quantity
) {}
