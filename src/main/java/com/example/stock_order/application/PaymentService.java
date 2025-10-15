package com.example.stock_order.application;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TokenizationService tokenization;

    public ChargeResult charge(String token, BigDecimal amount, String currency) {
        var rec = tokenization.validateToken(token); 
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("invalid_amount");
        }
        return new ChargeResult(UUID.randomUUID().toString(), "SUCCEEDED", amount, currency, rec.last4(), rec.brand());
    }

    public record ChargeResult(String chargeId, String status, BigDecimal amount, String currency,
                               String last4, String brand) {}
}
