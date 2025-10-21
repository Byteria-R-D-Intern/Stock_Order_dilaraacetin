package com.example.stock_order.adapters.web.dto.order;

import jakarta.validation.constraints.NotBlank;

public record CheckoutWithTokenRequest(
    @NotBlank String paymentToken
) {}