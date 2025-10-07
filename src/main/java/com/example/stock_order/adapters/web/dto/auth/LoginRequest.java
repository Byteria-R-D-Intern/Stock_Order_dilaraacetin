package com.example.stock_order.adapters.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Email formatı hatalı")
        @NotBlank(message = "Email alanı boş bırakılamaz")
        String email,

        @NotBlank(message = "Password alanı boş bırakılamaz")
        String password
) {}