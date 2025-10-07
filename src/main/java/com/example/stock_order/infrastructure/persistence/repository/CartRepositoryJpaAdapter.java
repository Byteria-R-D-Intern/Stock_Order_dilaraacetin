package com.example.stock_order.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stock_order.domain.model.Cart;
import com.example.stock_order.domain.ports.repository.CartRepository;
import com.example.stock_order.infrastructure.persistence.entity.CartEntity;
import com.example.stock_order.infrastructure.persistence.entity.CartItemEntity;
import com.example.stock_order.infrastructure.persistence.mapper.CartMapper;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataCartJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRepositoryJpaAdapter implements CartRepository {
    private final SpringDataCartJpaRepository jpa;

    @Override public Optional<Cart> findByUserId(Long userId){
        return jpa.findByUserId(userId).map(CartMapper::toDomain);
    }

    @Override public Cart save(Cart cart){
        CartEntity e = jpa.findByUserId(cart.getUserId()).orElseGet(() -> {
            CartEntity ne = new CartEntity();
            ne.setUserId(cart.getUserId());
            return ne;
        });
        
        e.getItems().clear();
        if (cart.getItems()!=null){
            for (var di : cart.getItems()){
                CartItemEntity ci = new CartItemEntity();
                ci.setCart(e);
                ci.setProductId(di.getProductId());
                ci.setSku(di.getSku());
                ci.setName(di.getName());
                ci.setUnitPrice(di.getUnitPrice());
                ci.setQuantity(di.getQuantity());
                e.getItems().add(ci);
            }
        }
        var saved = jpa.save(e);
        return CartMapper.toDomain(saved);
    }

    @Override public void deleteByUserId(Long userId){
        jpa.deleteByUserId(userId);
    }
}