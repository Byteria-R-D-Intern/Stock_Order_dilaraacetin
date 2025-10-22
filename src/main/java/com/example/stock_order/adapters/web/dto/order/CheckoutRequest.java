package com.example.stock_order.adapters.web.dto.order;

public record CheckoutRequest(
        String paymentToken,        
        Long savedPaymentMethodId,   
        Long shippingAddressId 
) {}
