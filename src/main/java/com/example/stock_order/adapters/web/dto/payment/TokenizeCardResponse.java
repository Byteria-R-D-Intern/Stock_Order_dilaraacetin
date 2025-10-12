package com.example.stock_order.adapters.web.dto.payment;

public record TokenizeCardResponse(String token, String last4, String brand, long expiresAtEpochMs) {}