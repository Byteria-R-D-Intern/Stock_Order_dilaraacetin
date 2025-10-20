package com.example.stock_order.adapters.web;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.order.CheckoutRequest;
import com.example.stock_order.adapters.web.dto.order.CheckoutResponse;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.application.CheckoutService;
import com.example.stock_order.application.OrderAdminService;
import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final CheckoutService checkoutService;
    private final OrderAdminService orderAdminService;
    private final ProductStockRepository stocks;
    private final AuditLogService audit;
    private final OrderRepository orders;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody @Valid CheckoutRequest req){
        var order = checkoutService.checkout(req.paymentToken());
        var resp = new CheckoutResponse(
            order.getId(),
            order.getStatus().name(),
            order.getTotalAmount(),
            order.getItems().stream().map(i ->
                new CheckoutResponse.Item(
                    i.getProductId(), i.getSku(), i.getName(),
                    i.getUnitPrice(), i.getQuantity(), i.getLineTotal()
                )
            ).collect(Collectors.toList())
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckoutResponse> get(@PathVariable Long id){
        var order = checkoutService.getMyOrder(id);
        var resp = new CheckoutResponse(
            order.getId(),
            order.getStatus().name(),
            order.getTotalAmount(),
            order.getItems().stream().map(i ->
                new CheckoutResponse.Item(
                    i.getProductId(), i.getSku(), i.getName(),
                    i.getUnitPrice(), i.getQuantity(), i.getLineTotal()
                )
            ).collect(Collectors.toList())
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<java.util.List<CheckoutResponse>> myOrders(){
        var list = checkoutService.listMyOrders().stream().map(order ->
            new CheckoutResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getItems().stream().map(i ->
                    new CheckoutResponse.Item(
                        i.getProductId(), i.getSku(), i.getName(),
                        i.getUnitPrice(), i.getQuantity(), i.getLineTotal()
                    )
                ).collect(Collectors.toList())
            )
        ).toList();
        return ResponseEntity.ok(list);
    }
    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam Order.Status status) {
        orderAdminService.changeStatus(id, status); 
        
        return ResponseEntity.ok().build();
    }
}
