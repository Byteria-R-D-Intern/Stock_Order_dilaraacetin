package com.example.stock_order.adapters.web.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Schema(example = "+905*********")
    @Size(max = 32)
    @NotBlank
    @Pattern(
        regexp = "^(\\+\\d{1,3}\\s?)?\\d{10,}$",
        message = "Geçerli bir telefon numarası girin"
    )
    String phoneNumber
) {}