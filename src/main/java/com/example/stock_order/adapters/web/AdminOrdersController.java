package com.example.stock_order.adapters.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.order.AdminOrderDetailResponse;
import com.example.stock_order.adapters.web.dto.order.AdminOrderSummaryResponse;
import com.example.stock_order.adapters.web.exception.NotFoundException;
import com.example.stock_order.application.OrderAdminService;
import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.ports.repository.OrderRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrdersController {

    private final OrderRepository orders;
    private final OrderAdminService orderAdminService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminOrderSummaryResponse>> list() {
        var list = orders.findAll().stream()
                .map(this::toSummary)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderDetailResponse> get(@PathVariable Long id) {
        var order = orders.findByIdWithItems(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        return ResponseEntity.ok(toDetail(order));
    }

    private AdminOrderSummaryResponse toSummary(Order o) {
        return new AdminOrderSummaryResponse(
                o.getId(),
                o.getUserId(),
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getTotalAmount(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }

    private AdminOrderDetailResponse toDetail(Order o) {
        List<AdminOrderDetailResponse.Item> items = o.getItems() == null ? List.of() :
                o.getItems().stream()
                        .map(i -> new AdminOrderDetailResponse.Item(
                                i.getProductId(),
                                i.getSku(),
                                i.getName(),
                                i.getUnitPrice(),
                                i.getQuantity(),
                                i.getLineTotal()))
                        .toList();

        return new AdminOrderDetailResponse(
                o.getId(),
                o.getUserId(),
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getTotalAmount(),
                o.getCreatedAt(),
                o.getUpdatedAt(),
                items
        );
    }
    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam Order.Status status) {
        orderAdminService.changeStatus(id, status); 
        
        return ResponseEntity.ok().build();
    }
}