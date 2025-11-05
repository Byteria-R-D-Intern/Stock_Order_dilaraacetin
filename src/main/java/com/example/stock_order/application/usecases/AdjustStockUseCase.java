package com.example.stock_order.application.usecases;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.adapters.web.exception.NotFoundException;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.domain.model.Product;
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

    private static final long AUDIT_STOCK_THRESHOLD = 50L;

    @Transactional
    public ProductStock increase(Long productId, long delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta > 0 olmalı");

        var product = products.findById(productId)
                .orElseThrow(() -> new NotFoundException("ürün bulunamadı"));

        var stock = stocks.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("stok bulunamadı"));

        stock.setQuantityOnHand(stock.getQuantityOnHand() + delta);
        var saved = stocks.save(stock);

        if (product.getStatus() == Product.Status.INACTIVE) {
            product.setStatus(Product.Status.ACTIVE);
            products.save(product);
        }

        if (delta > AUDIT_STOCK_THRESHOLD) {
            audit.log(
                    "STOCK_INCREASED",
                    "PRODUCT",
                    productId,
                    Map.of(
                            "delta", delta,
                            "newQty", saved.getQuantityOnHand()
                    )
            );
        }

        return saved;
    }

    @Transactional
    public ProductStock decrease(Long productId, long delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta > 0");

        var product = products.findById(productId)
                .orElseThrow(() -> new NotFoundException("ürün bulunamadı"));

        var stock = stocks.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("stok bulunamadı"));

        long newQty = stock.getQuantityOnHand() - delta;
        if (newQty < 0) throw new IllegalStateException("yetersiz stok miktarı");

        stock.setQuantityOnHand(newQty);
        var saved = stocks.save(stock);

        if (newQty == 0 && product.getStatus() != Product.Status.INACTIVE) {
            product.setStatus(Product.Status.INACTIVE);
            products.save(product);
        }

        if (delta > AUDIT_STOCK_THRESHOLD) {
            audit.log(
                    "STOCK_DECREASED",
                    "PRODUCT",
                    productId,
                    Map.of(
                            "delta", delta,
                            "newQty", saved.getQuantityOnHand()
                    )
            );
        }

        return saved;
    }
}
