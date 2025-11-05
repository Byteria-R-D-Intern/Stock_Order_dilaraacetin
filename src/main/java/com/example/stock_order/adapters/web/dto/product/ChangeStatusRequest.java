package com.example.stock_order.adapters.web.dto.product;

import com.example.stock_order.domain.model.Product;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull Product.Status status
) {}
