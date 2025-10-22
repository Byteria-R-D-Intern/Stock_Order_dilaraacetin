package com.example.stock_order.adapters.web.dto.order;

import jakarta.validation.constraints.NotNull;

public record CheckoutWithSavedMethodRequest(
    @NotNull Long savedPaymentMethodId,
    @NotNull Long shippingAddressId 
) {}