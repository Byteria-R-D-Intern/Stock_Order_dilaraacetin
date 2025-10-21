package com.example.stock_order.adapters.web;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.order.CheckoutResponse;
import com.example.stock_order.adapters.web.dto.order.CheckoutWithSavedMethodRequest;
import com.example.stock_order.adapters.web.dto.order.CheckoutWithTokenRequest;
import com.example.stock_order.application.CheckoutService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final CheckoutService checkoutService;

    @PostMapping("/checkout/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutResponse> checkoutWithToken(@RequestBody @Valid CheckoutWithTokenRequest req){
        var order = checkoutService.checkoutWithToken(req.paymentToken());
        var resp = new CheckoutResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getItems().stream().map(i -> new CheckoutResponse.Item(
                        i.getProductId(),
                        i.getSku(),
                        i.getName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()
                )).collect(Collectors.toList())
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/checkout/saved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutResponse> checkoutWithSaved(@RequestBody @Valid CheckoutWithSavedMethodRequest req){
        var order = checkoutService.checkoutWithSaved(req.savedPaymentMethodId());
        var resp = new CheckoutResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getItems().stream().map(i -> new CheckoutResponse.Item(
                        i.getProductId(),
                        i.getSku(),
                        i.getName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()
                )).collect(Collectors.toList())
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
                order.getItems().stream().map(i -> new CheckoutResponse.Item(
                        i.getProductId(),
                        i.getSku(),
                        i.getName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()
                )).collect(Collectors.toList())
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<java.util.List<CheckoutResponse>> myOrders(){
        var list = checkoutService.listMyOrders().stream().map(order -> new CheckoutResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getItems().stream().map(i -> new CheckoutResponse.Item(
                        i.getProductId(),
                        i.getSku(),
                        i.getName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()
                )).collect(java.util.stream.Collectors.toList())
        )).toList();
        return ResponseEntity.ok(list);
    }
}
