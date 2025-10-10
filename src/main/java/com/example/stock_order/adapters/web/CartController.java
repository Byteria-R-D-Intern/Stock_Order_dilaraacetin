package com.example.stock_order.adapters.web;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.cart.AddToCartRequest;
import com.example.stock_order.adapters.web.dto.cart.CartItemResponse;
import com.example.stock_order.adapters.web.dto.cart.CartResponse;
import com.example.stock_order.adapters.web.dto.cart.UpdateQtyRequest;
import com.example.stock_order.application.CartService;
import com.example.stock_order.domain.ports.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository users;

    private Long currentUserId(Authentication auth){
        String email = (String) auth.getPrincipal();
        return users.findByEmail(email).map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("kullanıcı bulunamadı"));
    }

    @GetMapping
    public ResponseEntity<CartResponse> get(Authentication auth){
        var cart = cartService.getOrCreate(currentUserId(auth));
        var resp = new CartResponse(
            cart.getUserId(),
            cart.getItems().stream().map(i ->
                new CartItemResponse(i.getProductId(), i.getSku(), i.getName(), i.getUnitPrice(), i.getQuantity())
            ).collect(Collectors.toList())
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> add(Authentication auth, @RequestBody @Valid AddToCartRequest req){
        var cart = cartService.addItem(currentUserId(auth), req.productId(), req.quantity());
        var resp = new CartResponse(
            cart.getUserId(),
            cart.getItems().stream().map(i ->
                new CartItemResponse(i.getProductId(), i.getSku(), i.getName(), i.getUnitPrice(), i.getQuantity())
            ).collect(Collectors.toList())
        );
        return ResponseEntity.ok(resp);
    }
    @PutMapping("/items")
    public ResponseEntity<CartResponse> updateQty(Authentication auth, @RequestBody @Valid UpdateQtyRequest req){
        var cart = cartService.updateQuantity(currentUserId(auth), req.productId(), req.quantity());
        var resp = new CartResponse(
            cart.getUserId(),
            cart.getItems().stream().map(i ->
                new CartItemResponse(i.getProductId(), i.getSku(), i.getName(), i.getUnitPrice(), i.getQuantity())
            ).collect(Collectors.toList())
        );
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication auth){
        cartService.clear(currentUserId(auth));
        return ResponseEntity.ok().build();   
    }
}
