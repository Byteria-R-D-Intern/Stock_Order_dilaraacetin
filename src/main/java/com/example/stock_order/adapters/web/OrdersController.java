package com.example.stock_order.adapters.web;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.order.CheckoutRequest;
import com.example.stock_order.adapters.web.dto.order.CheckoutResponse;
import com.example.stock_order.application.CheckoutService;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.domain.ports.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final CheckoutService checkoutService;
    private final UserRepository users;
    private final OrderRepository orders;

    private Long currentUserId(Authentication auth){
        String email = (String) auth.getPrincipal();
        return users.findByEmail(email).map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("kullanıcı bulunamadı"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(Authentication auth, @RequestBody @Valid CheckoutRequest req){
        var order = checkoutService.checkout(currentUserId(auth), req.paymentToken());
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
    public ResponseEntity<CheckoutResponse> get(Authentication auth, @PathVariable Long id){
        var userId = currentUserId(auth);
        var order = orders.findById(id).orElseThrow(() -> new IllegalArgumentException("sipariş bulunamadı"));
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
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
    public ResponseEntity<java.util.List<CheckoutResponse>> myOrders(Authentication auth){
        var userId = currentUserId(auth);
        var list = orders.findByUserId(userId).stream().map(order ->
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
}
