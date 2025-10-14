package com.example.stock_order.adapters.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.product.ProductResponse;
import com.example.stock_order.domain.ports.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductRepository products;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> listActive() {
        var list = products.findAllActive().stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable Long id) {
        return products.findById(id)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}