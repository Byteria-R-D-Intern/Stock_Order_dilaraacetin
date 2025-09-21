package com.example.stock_order.domain.ports.repository;

import java.util.Optional;

import com.example.stock_order.domain.model.ProductStock;

public interface ProductStockRepository {
    Optional<ProductStock> findByProductId(Long productId);
    ProductStock save(ProductStock stock);
}