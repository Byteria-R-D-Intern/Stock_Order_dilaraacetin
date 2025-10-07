package com.example.stock_order.application.usecases;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.domain.model.ProductStock;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdjustStockUseCase {
    private final ProductRepository products;
    private final ProductStockRepository stocks;
    private final AuditLogService audit;

    @Transactional
    public ProductStock increase(Long productId, long delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta must be > 0");
        products.findById(productId).orElseThrow(() -> new IllegalArgumentException("product not found"));
        var stock = stocks.findByProductId(productId).orElseThrow(() -> new IllegalStateException("stock row missing"));
        stock.setQuantityOnHand(stock.getQuantityOnHand() + delta);
        var saved = stocks.save(stock);

        audit.log("STOCK_INCREASED", "PRODUCT", productId, Map.of("delta", delta, "newQty", saved.getQuantityOnHand()));
        return saved;
    }

    @Transactional
    public ProductStock decrease(Long productId, long delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta must be > 0");
        products.findById(productId).orElseThrow(() -> new IllegalArgumentException("product not found"));
        var stock = stocks.findByProductId(productId).orElseThrow(() -> new IllegalStateException("stock row missing"));
        long newQty = stock.getQuantityOnHand() - delta;
        if (newQty < 0) throw new IllegalStateException("insufficient stock");
        stock.setQuantityOnHand(newQty);
        var saved = stocks.save(stock);

        audit.log("STOCK_DECREASED", "PRODUCT", productId, Map.of("delta", delta, "newQty", saved.getQuantityOnHand()));
        return saved;
    }
}