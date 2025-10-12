package com.example.stock_order.adapters.web.dto.payment;

import java.math.BigDecimal;

public record ChargeResponse(
        String chargeId,
        String status,     
        BigDecimal amount,
        String currency
) {}