package com.example.stock_order.adapters.web.dto.payment;

public record SavedPaymentMethodResponse(
        Long   id,
        String last4,
        String brand,
        String expiryMonth,
        String expiryYear
) {}
