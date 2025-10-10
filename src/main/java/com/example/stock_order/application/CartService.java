package com.example.stock_order.application;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.model.Cart;
import com.example.stock_order.domain.model.CartItem;
import com.example.stock_order.domain.ports.repository.CartRepository;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository carts;
    private final ProductRepository products;
    private final ProductStockRepository stocks;

    @Transactional
    public Cart getOrCreate(Long userId) {
        return carts.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            c.setItems(new ArrayList<>());
            return carts.save(c);
        });
    }

    @Transactional
    public Cart addItem(Long userId, Long productId, Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity > 0 olmalı");
        }

        var p = products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("ürün bulunamadı"));

        var stock = stocks.findByProductId(productId)
                .orElseThrow(() -> new IllegalStateException("stok bilgisi bulunamadı"));

        var cart = getOrCreate(userId);

        long currentQtyInCart = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .mapToLong(CartItem::getQuantity)
                .sum();

        long desiredTotal = currentQtyInCart + quantity;
        if (desiredTotal > stock.getQuantityOnHand()) {
            throw new IllegalArgumentException("yetersiz stok miktarı");
        }

        var existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(desiredTotal);
        } else {
            CartItem ci = new CartItem();
            ci.setProductId(productId);
            ci.setSku(p.getSku());
            ci.setName(p.getName());
            ci.setUnitPrice(p.getCurrentPrice()); 
            ci.setQuantity(quantity);
            cart.getItems().add(ci);
        }

        return carts.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long userId, Long productId, Long quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("quantity >= 0 olmalı");
        }

        var p = products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("ürün bulunamadı"));
        var stock = stocks.findByProductId(productId)
                .orElseThrow(() -> new IllegalStateException("stok bilgisi bulunamadı"));

        var cart = getOrCreate(userId);

        var itemOpt = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (itemOpt.isEmpty()) {
            if (quantity == 0) {
                return cart;
            }
            if (quantity > stock.getQuantityOnHand()) {
                throw new IllegalArgumentException("yetersiz stok miktarı");
            }
            CartItem ci = new CartItem();
            ci.setProductId(productId);
            ci.setSku(p.getSku());
            ci.setName(p.getName());
            ci.setUnitPrice(p.getCurrentPrice());
            ci.setQuantity(quantity);
            cart.getItems().add(ci);
            return carts.save(cart);
        }

        if (quantity == 0) {
            Iterator<CartItem> it = cart.getItems().iterator();
            while (it.hasNext()) {
                if (it.next().getProductId().equals(productId)) {
                    it.remove();
                    break;
                }
            }
            return carts.save(cart);
        }

        if (quantity > stock.getQuantityOnHand()) {
            throw new IllegalArgumentException("yetersiz stok miktarı");
        }

        var item = itemOpt.get();
        item.setQuantity(quantity);

        return carts.save(cart);
    }

    @Transactional
    public void clear(Long userId) {
        carts.deleteByUserId(userId);
    }
}