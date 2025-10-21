package com.example.stock_order.adapters.web.dto.order;

public record CheckoutRequest(
        String paymentToken,        // one-shot token (opsiyonel)
        Long savedPaymentMethodId   // kayıtlı yöntem id (opsiyonel)
) {}
