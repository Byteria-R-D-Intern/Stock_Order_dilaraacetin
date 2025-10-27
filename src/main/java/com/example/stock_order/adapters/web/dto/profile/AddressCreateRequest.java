package com.example.stock_order.adapters.web.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(
        @NotBlank @Size(max = 64)  String title,
        @NotBlank @Size(max = 128) String recipientName,
        @NotBlank @Size(max = 255) String line1,
                   @Size(max = 255) String line2,                 
        @NotBlank @Size(max = 128) String city,
                   @Size(max = 128) String state,                 
                   @Size(max = 32)  String postalCode,           
        @NotBlank @Size(max = 64)  String country,

        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "^[0-9+\\-()\\s]{7,32}$", message = "invalid_phone")
        String phone,

        @NotNull
        @JsonProperty("isDefault")
        Boolean isDefault
) {}
