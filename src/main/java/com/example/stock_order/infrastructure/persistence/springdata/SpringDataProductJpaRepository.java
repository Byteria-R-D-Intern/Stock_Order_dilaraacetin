package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.ProductEntity;
import com.example.stock_order.infrastructure.persistence.entity.ProductEntity.Status;

public interface SpringDataProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findBySku(String sku);
    List<ProductEntity> findAllByStatus(Status status);
}