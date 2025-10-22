package com.example.stock_order.adapters.web.dto.profile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 32)
    @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "invalid_phone")
    String phoneNumber
) {}
