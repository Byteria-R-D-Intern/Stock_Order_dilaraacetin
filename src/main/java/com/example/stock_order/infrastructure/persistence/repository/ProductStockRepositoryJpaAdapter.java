package com.example.stock_order.infrastructure.persistence.repository;


import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stock_order.domain.model.ProductStock;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;
import com.example.stock_order.infrastructure.persistence.entity.ProductEntity;
import com.example.stock_order.infrastructure.persistence.mapper.ProductStockMapper;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataProductJpaRepository;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataProductStockJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductStockRepositoryJpaAdapter implements ProductStockRepository {

    private final SpringDataProductStockJpaRepository stockJpa;
    private final SpringDataProductJpaRepository productJpa;

    @Override
    public Optional<ProductStock> findByProductId(Long productId) {
        return stockJpa.findById(productId).map(ProductStockMapper::toDomain);
    }

    @Override
    public ProductStock save(ProductStock stock) {
        var entity = ProductStockMapper.toEntity(stock);
        ProductEntity productRef = productJpa.getReferenceById(stock.getProductId());
        entity.setProduct(productRef);

        var saved = stockJpa.save(entity);
        return ProductStockMapper.toDomain(saved);
    }
}
