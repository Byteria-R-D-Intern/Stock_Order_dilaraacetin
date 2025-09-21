package com.example.stock_order.infrastructure.persistence.mapper;


import com.example.stock_order.domain.model.ProductStock;
import com.example.stock_order.infrastructure.persistence.entity.ProductStockEntity;

public final class ProductStockMapper {
    private ProductStockMapper(){}

    public static ProductStock toDomain(ProductStockEntity e) {
        if (e == null) return null;
        ProductStock d = new ProductStock();
        d.setProductId(e.getProductId());
        d.setQuantityOnHand(e.getQuantityOnHand());
        d.setVersion(e.getVersion());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    public static ProductStockEntity toEntity(ProductStock d) {
        if (d == null) return null;
        ProductStockEntity e = new ProductStockEntity();
        e.setProductId(d.getProductId()); 
        e.setQuantityOnHand(d.getQuantityOnHand());
        e.setVersion(d.getVersion());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
