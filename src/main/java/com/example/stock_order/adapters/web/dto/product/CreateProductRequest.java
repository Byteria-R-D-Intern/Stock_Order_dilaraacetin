package com.example.stock_order.adapters.web.dto.product;

import java.math.BigDecimal;

import com.example.stock_order.domain.model.Product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank(message = "sku alanı boş olamaz")
        @Size(max = 64, message = "sku en fazla 64 karakter")
        String sku,

        @NotBlank(message = "name alanı boş olamaz")
        @Size(max = 255, message = "name en fazla 255 karakter")
        String name,

        @Size(max = 10_000, message = "description çok uzun")
        String description,

        @NotNull(message = "price alanı zorunlu")
        @DecimalMin(value = "0.01", inclusive = true, message = "price > 0 olmalı")
        @Digits(integer = 10, fraction = 2, message = "price 2 ondalık hassasiyetinde olmalı")
        BigDecimal price,

        Product.Status status, 

        @PositiveOrZero(message = "initialQuantity negatif olamaz")
        Long initialQuantity
) {}
