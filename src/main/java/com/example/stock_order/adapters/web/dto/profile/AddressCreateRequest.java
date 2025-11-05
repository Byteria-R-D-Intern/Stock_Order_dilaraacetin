package com.example.stock_order.adapters.web.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(example = "+905*********")
        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "^(\\+\\d{1,3}\\s?)?\\d{10,}$", message = "Geçerli bir telefon numarası girin")
        String phone,

        @NotNull
        @JsonProperty("isDefault")
        Boolean isDefault
) {}
