package com.example.stock_order.domain.ports.repository;

import com.example.stock_order.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    Optional<Product> findBySku(String sku);
    List<Product> findAllActive();
    Product save(Product product);
}