package com.example.stock_order.adapters.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Email formatı hatalı")
        @NotBlank(message = "Email alanı boş bırakılamaz")
        String email,

        @NotBlank(message = "Password alanı boş bırakılamaz")
        @Size(min = 8, max = 25, message = "Password uzunluğu 8-25 karakter olmalı")
        /* 
        @Pattern(
           regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
           message = "{Şifre en az 1 küçük, 1 büyük harf ve 1 rakam içermelidir}"
        )
        */
        String password
) {}