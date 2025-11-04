package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.PaymentTokenEntity;

public interface PaymentTokenJpaRepository extends JpaRepository<PaymentTokenEntity, Long> {
    Optional<PaymentTokenEntity> findByToken(String token);
}
