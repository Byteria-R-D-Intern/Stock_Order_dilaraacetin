package com.example.stock_order.adapters.web.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutWithTokenRequest(
    @NotBlank String paymentToken,
    @NotNull  Long shippingAddressId
) {}