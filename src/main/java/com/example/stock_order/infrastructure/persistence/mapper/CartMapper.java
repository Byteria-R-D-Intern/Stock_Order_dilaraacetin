package com.example.stock_order.infrastructure.persistence.mapper;

import java.util.stream.Collectors;

import com.example.stock_order.domain.model.Cart;
import com.example.stock_order.domain.model.CartItem;
import com.example.stock_order.infrastructure.persistence.entity.CartEntity;
import com.example.stock_order.infrastructure.persistence.entity.CartItemEntity;

public final class CartMapper {
    private CartMapper(){}

    public static Cart toDomain(CartEntity e){
        if(e==null) return null;
        Cart d = new Cart();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setItems(e.getItems().stream().map(CartMapper::toDomain).collect(Collectors.toList()));
        return d;
    }
    public static CartItem toDomain(CartItemEntity e){
        if(e==null) return null;
        CartItem d = new CartItem();
        d.setId(e.getId());
        d.setCartId(e.getCart().getId());
        d.setProductId(e.getProductId());
        d.setSku(e.getSku());
        d.setName(e.getName());
        d.setUnitPrice(e.getUnitPrice());
        d.setQuantity(e.getQuantity());
        return d;
    }

    public static CartEntity toEntity(Cart d){
        if(d==null) return null;
        CartEntity e = new CartEntity();
        e.setId(d.getId());
        e.setUserId(d.getUserId());
        return e;
    }
}