package com.example.stock_order.infrastructure.persistence.mapper;

import com.example.stock_order.domain.model.UserAddress;
import com.example.stock_order.infrastructure.persistence.entity.UserAddressEntity;

public final class UserAddressMapper {
    private UserAddressMapper(){}

    public static UserAddress toDomain(UserAddressEntity e){
        if(e==null) return null;
        UserAddress d = new UserAddress();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setTitle(e.getTitle());
        d.setRecipientName(e.getRecipientName());
        d.setLine1(e.getLine1());
        d.setLine2(e.getLine2());
        d.setCity(e.getCity());
        d.setState(e.getState());
        d.setPostalCode(e.getPostalCode());
        d.setCountry(e.getCountry());
        d.setPhone(e.getPhone());
        d.setDefault(e.isDefault());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    public static UserAddressEntity toEntity(UserAddress d){
        if(d==null) return null;
        UserAddressEntity e = new UserAddressEntity();
        e.setId(d.getId());
        e.setUserId(d.getUserId());
        e.setTitle(d.getTitle());
        e.setRecipientName(d.getRecipientName());
        e.setLine1(d.getLine1());
        e.setLine2(d.getLine2());
        e.setCity(d.getCity());
        e.setState(d.getState());
        e.setPostalCode(d.getPostalCode());
        e.setCountry(d.getCountry());
        e.setPhone(d.getPhone());
        e.setDefault(d.isDefault());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
