package com.example.stock_order.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stock_order.domain.model.Product;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.infrastructure.persistence.entity.ProductEntity;
import com.example.stock_order.infrastructure.persistence.mapper.ProductMapper;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataProductJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryJpaAdapter implements ProductRepository {

    private final SpringDataProductJpaRepository jpa;

    @Override
    public Optional<Product> findById(Long id) {
        return jpa.findById(id).map(ProductMapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpa.findBySku(sku).map(ProductMapper::toDomain);
    }

    @Override
    public List<Product> findAllActive() {
        return jpa.findAllByStatus(ProductEntity.Status.ACTIVE)
                  .stream()
                  .map(ProductMapper::toDomain)
                  .toList();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() != null) {
            var e = jpa.findById(product.getId())
                       .orElseThrow(() -> new IllegalArgumentException("product not found: " + product.getId()));

            e.setSku(product.getSku());
            e.setName(product.getName());
            e.setDescription(product.getDescription());
            e.setCurrentPrice(product.getCurrentPrice());
            e.setStatus(ProductEntity.Status.valueOf(product.getStatus().name()));

            var saved = jpa.save(e);
            return ProductMapper.toDomain(saved);
        } else {
            var saved = jpa.save(ProductMapper.toEntity(product));
            return ProductMapper.toDomain(saved);
        }
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
