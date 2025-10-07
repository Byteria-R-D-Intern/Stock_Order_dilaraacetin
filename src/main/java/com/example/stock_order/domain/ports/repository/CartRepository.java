package com.example.stock_order.domain.ports.repository;

import java.util.Optional;
import com.example.stock_order.domain.model.Cart;

public interface CartRepository {
    Optional<Cart> findByUserId(Long userId);
    Cart save(Cart cart);
    void deleteByUserId(Long userId);
}