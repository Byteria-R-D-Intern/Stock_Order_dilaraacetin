package com.example.stock_order.adapters.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.product.AdjustStockRequest;
import com.example.stock_order.adapters.web.dto.product.CreateProductRequest;
import com.example.stock_order.adapters.web.dto.product.ProductResponse;
import com.example.stock_order.adapters.web.dto.product.StockResponse;
import com.example.stock_order.application.usecases.AdjustStockUseCase;
import com.example.stock_order.application.usecases.CreateProductUseCase;
import com.example.stock_order.domain.model.Product;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final CreateProductUseCase createProduct;
    private final AdjustStockUseCase adjustStock;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@RequestBody @Valid CreateProductRequest req) {
        var p = createProduct.handle(
                req.sku(),
                req.name(),
                req.description(),
                req.price(),
                req.status() != null ? req.status() : Product.Status.ACTIVE,
                req.initialQuantity()
        );
        return ResponseEntity.ok(ProductResponse.from(p));
    }

    @PostMapping("/{id}/stock/increase")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockResponse> increase(@PathVariable Long id,
                                                  @RequestBody @Valid AdjustStockRequest req) {
        var s = adjustStock.increase(id, req.delta());
        return ResponseEntity.ok(StockResponse.from(s));
    }

    @PostMapping("/{id}/stock/decrease")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockResponse> decrease(@PathVariable Long id,
                                                  @RequestBody @Valid AdjustStockRequest req) {
        var s = adjustStock.decrease(id, req.delta());
        return ResponseEntity.ok(StockResponse.from(s));
    }
}