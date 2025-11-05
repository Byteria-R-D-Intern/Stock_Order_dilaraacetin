package com.example.stock_order.domain.ports.repository;

import java.util.List;
import java.util.Optional;

import com.example.stock_order.domain.model.Product;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    Optional<Product> findBySku(String sku);
    List<Product> findAllActive();
    Product save(Product product);
    void deleteById(Long id);
    List<Product> findAll(); 
}