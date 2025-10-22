package com.example.stock_order.adapters.web.dto.profile;

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
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "invalid_phone")
    String phone,
    Boolean isDefault
) {}
