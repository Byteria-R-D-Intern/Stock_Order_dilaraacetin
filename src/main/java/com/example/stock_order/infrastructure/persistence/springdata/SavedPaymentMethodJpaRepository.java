package com.example.stock_order.infrastructure.persistence.springdata;

import com.example.stock_order.infrastructure.persistence.entity.SavedPaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedPaymentMethodJpaRepository extends JpaRepository<SavedPaymentMethodEntity, Long> {
    List<SavedPaymentMethodEntity> findByUserIdAndActiveTrue(Long userId);
    Optional<SavedPaymentMethodEntity> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
}
