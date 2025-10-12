package com.example.stock_order.adapters.web.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TokenizeCardRequest(
        @NotBlank @Size(min = 12, max = 19) String cardNumber, 
        @Pattern(regexp = "^(0[1-9]|1[0-2])$") String expiryMonth, 
        @Pattern(regexp = "^[0-9]{2}$") String expiryYear,         
        @NotBlank @Size(min = 3, max = 4) String cvv
) {}