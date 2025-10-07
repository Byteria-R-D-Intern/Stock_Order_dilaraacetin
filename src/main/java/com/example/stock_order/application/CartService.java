package com.example.stock_order.application;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.model.Cart;
import com.example.stock_order.domain.model.CartItem;
import com.example.stock_order.domain.ports.repository.CartRepository;
import com.example.stock_order.domain.ports.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository carts;
    private final ProductRepository products;

    @Transactional   
    public Cart getOrCreate(Long userId){
        return carts.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return carts.save(c);
        });
    }

    @Transactional
    public Cart addItem(Long userId, Long productId, long quantity){
        if(quantity <= 0) throw new IllegalArgumentException("quantity > 0 olmalı");

        var product = products.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("ürün bulunamadı"));

        Cart cart = getOrCreate(userId);

        Optional<CartItem> existing = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst();

        if (existing.isPresent()){
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem ci = new CartItem();
            ci.setProductId(productId);
            ci.setSku(product.getSku());
            ci.setName(product.getName());
            ci.setUnitPrice(product.getCurrentPrice() != null ? product.getCurrentPrice() : BigDecimal.ZERO);
            ci.setQuantity(quantity);
            cart.getItems().add(ci);
        }
        return carts.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long userId, Long productId, long quantity){
        if(quantity < 0) throw new IllegalArgumentException("quantity >= 0 olmalı");
        Cart cart = getOrCreate(userId);
        var it = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("ürün sepette bulunmuyor"));

        if (quantity == 0){
            cart.getItems().remove(it);
        } else {
            it.setQuantity(quantity);
        }
        return carts.save(cart);
    }

    @Transactional
    public void clear(Long userId){
        carts.deleteByUserId(userId);
    }
}