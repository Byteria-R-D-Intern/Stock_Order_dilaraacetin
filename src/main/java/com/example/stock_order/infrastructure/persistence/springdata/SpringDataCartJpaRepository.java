package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.CartEntity;

public interface SpringDataCartJpaRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}