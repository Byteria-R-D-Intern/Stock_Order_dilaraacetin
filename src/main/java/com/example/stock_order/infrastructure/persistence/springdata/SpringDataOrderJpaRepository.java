package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.OrderEntity;

public interface SpringDataOrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserId(Long userId);
}