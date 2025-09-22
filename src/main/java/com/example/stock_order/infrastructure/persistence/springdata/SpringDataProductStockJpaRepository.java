package com.example.stock_order.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.ProductStockEntity;

public interface SpringDataProductStockJpaRepository extends JpaRepository<ProductStockEntity, Long> {
}

