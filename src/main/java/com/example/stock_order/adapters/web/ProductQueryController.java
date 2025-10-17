package com.example.stock_order.adapters.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.product.ProductResponse;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductRepository products;
    private final ProductStockRepository stocks;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> listActive() {
        var list = products.findAllActive().stream()
                .map(p -> {
                    Long qoh = stocks.findByProductId(p.getId())
                            .map(s -> s.getQuantityOnHand())
                            .orElse(0L);
                    return ProductResponse.of(p, qoh);
                })
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable Long id) {
        return products.findById(id)
                .map(p -> {
                    Long qoh = stocks.findByProductId(p.getId())
                            .map(s -> s.getQuantityOnHand())
                            .orElse(0L);
                    return ProductResponse.of(p, qoh);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
