package com.example.stock_order.infrastructure.persistence.mapper;

import com.example.stock_order.domain.model.Product;
import com.example.stock_order.infrastructure.persistence.entity.ProductEntity;

public final class ProductMapper {
    private ProductMapper() {}

    public static Product toDomain(ProductEntity e) {
        if (e == null) return null;
        Product d = new Product();
        d.setId(e.getId());
        d.setSku(e.getSku());
        d.setName(e.getName());
        d.setDescription(e.getDescription());
        d.setCurrentPrice(e.getCurrentPrice());
        d.setStatus(Product.Status.valueOf(e.getStatus().name()));
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    public static ProductEntity toEntity(Product d) {
        if (d == null) return null;
        ProductEntity e = new ProductEntity();
        e.setId(d.getId());
        e.setSku(d.getSku());
        e.setName(d.getName());
        e.setDescription(d.getDescription());
        e.setCurrentPrice(d.getCurrentPrice());
        e.setStatus(ProductEntity.Status.valueOf(d.getStatus().name()));
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}