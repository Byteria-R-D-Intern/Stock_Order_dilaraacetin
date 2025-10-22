package com.example.stock_order.adapters.web.dto.profile;

import com.example.stock_order.domain.model.UserAddress;

public record AddressResponse(
    Long id, String title, String recipientName,
    String line1, String line2, String city, String state,
    String postalCode, String country, String phone, boolean isDefault
){
    public static AddressResponse of(UserAddress a){
        return new AddressResponse(
            a.getId(), a.getTitle(), a.getRecipientName(),
            a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
            a.getPostalCode(), a.getCountry(), a.getPhone(), a.isDefault()
        );
    }
}
