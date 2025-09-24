package com.example.stock_order.adapters.web.dto.product;

import jakarta.validation.constraints.Positive;

public record AdjustStockRequest(
        @Positive(message = "delta > 0 olmalı")
        long delta
) {}