package com.example.stock_order.application;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.model.Product;
import com.example.stock_order.domain.model.ProductStock;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {
    private final ProductRepository products;
    private final ProductStockRepository stocks;

    @Transactional
    public Product handle(String sku, String name, String description, BigDecimal price, Product.Status status, Long initialQty) {
        products.findBySku(sku).ifPresent(p -> { throw new IllegalArgumentException("SKU already exists"); });

        var p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setDescription(description);
        p.setCurrentPrice(price);
        p.setStatus(status);
        var saved = products.save(p);

        var s = new ProductStock();
        s.setProductId(saved.getId());
        s.setQuantityOnHand(initialQty != null ? Math.max(0, initialQty) : 0L);
        stocks.save(s);

        return saved;
    }
}