package com.example.stock_order.adapters.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.product.AdjustStockRequest;
import com.example.stock_order.adapters.web.dto.product.CreateProductRequest;
import com.example.stock_order.adapters.web.dto.product.ProductResponse;
import com.example.stock_order.adapters.web.dto.product.ProductUpdateRequest;
import com.example.stock_order.adapters.web.dto.product.StockResponse;
import com.example.stock_order.adapters.web.exception.NotFoundException;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.application.usecases.AdjustStockUseCase;
import com.example.stock_order.application.usecases.CreateProductUseCase;
import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.model.Product;
import com.example.stock_order.domain.model.ProductStock;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

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
    private final ProductRepository products;
    private final ProductStockRepository stocks;
    private final AuditLogService audit;
    private final OrderRepository orders;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listActiveWithStock() {
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

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        var p = products.findById(id).orElseThrow(() -> new NotFoundException("product not found"));
        Long qoh = stocks.findByProductId(id).map(s -> s.getQuantityOnHand()).orElse(0L);
        return ResponseEntity.ok(ProductResponse.of(p, qoh));
    }

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
        Long qoh = stocks.findByProductId(p.getId()).map(s -> s.getQuantityOnHand()).orElse(0L);
        return ResponseEntity.ok(ProductResponse.of(p, qoh));
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @RequestBody @Valid ProductUpdateRequest req) {
        var p = products.findById(id).orElseThrow(() -> new NotFoundException("product not found"));

        if (req.name() != null)        p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        if (req.price() != null)       p.setCurrentPrice(req.price());
        if (req.status() != null)      p.setStatus(req.status());

        var saved = products.save(p);
        Long qoh = stocks.findByProductId(id).map(s -> s.getQuantityOnHand()).orElse(0L);

        audit.log("PRODUCT_UPDATED", "PRODUCT", id,
                Map.of("name", saved.getName(), "price", saved.getCurrentPrice(),
                       "status", saved.getStatus() != null ? saved.getStatus().name() : null));

        return ResponseEntity.ok(ProductResponse.of(saved, qoh));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        products.findById(id).orElseThrow(() -> new NotFoundException("product not found"));
        stocks.deleteByProductId(id);
        products.deleteById(id);
        audit.log("PRODUCT_DELETED", "PRODUCT", id, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id,
                                             @RequestParam Order.Status status) {
        var orderOpt = orders.findById(id);
        if (orderOpt.isEmpty()) return ResponseEntity.notFound().build();

        var order = orderOpt.get();
        var prev = order.getStatus();

        if (prev != Order.Status.CANCELLED && status == Order.Status.CANCELLED) {
            for (var it : order.getItems()) {
                ProductStock s = stocks.findByProductId(it.getProductId())
                        .orElseThrow(() -> new IllegalStateException("stock row missing: " + it.getProductId()));
                s.setQuantityOnHand(s.getQuantityOnHand() + it.getQuantity());
                stocks.save(s);
            }
        }

        order.setStatus(status);
        orders.save(order);

        audit.log("ORDER_STATUS_CHANGED", "ORDER", order.getId(),
                Map.of("previous", prev.name(), "current", status.name()));
        return ResponseEntity.ok().build();
    }
}