package com.example.stock_order.adapters.web.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(
    @Size(max=64)  String title,
    @Size(max=128) String recipientName,
    @Size(max=255) String line1,
    @Size(max=255) String line2,
    @Size(max=128) String city,
    @Size(max=128) String state,
    @Size(max=32)  String postalCode,
    @Size(max=64)  String country,

    @Size(max=32)
    @Schema(example = "+905*********")
    @Pattern(regexp = "^(\\+\\d{1,3}\\s?)?\\d{10,}$", message = "Geçerli bir telefon numarası girin")
    String phone,

    Boolean isDefault
) {}
