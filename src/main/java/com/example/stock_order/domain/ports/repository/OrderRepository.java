package com.example.stock_order.domain.ports.repository;

import java.util.List;
import java.util.Optional;

import com.example.stock_order.domain.model.Order;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
}