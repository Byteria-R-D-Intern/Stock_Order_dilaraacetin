package com.example.stock_order.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.model.OrderItem;
import com.example.stock_order.domain.model.Product;
import com.example.stock_order.domain.model.ProductStock;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final int MAX_RETRY = 3;

    private final CartService cartService;
    private final ProductRepository products;
    private final ProductStockRepository stocks;
    private final OrderRepository orders;
    private final AuditLogService audit;

    private void validatePaymentToken(String token){
        if (token == null || token.isBlank() || token.length() < 8) {
            throw new IllegalArgumentException("invalid payment token");
        }
    }

    @Transactional
    public Order checkout(Long userId, String paymentToken){
        validatePaymentToken(paymentToken);

        var cart = cartService.getOrCreate(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var ci : cart.getItems()){
            Product p = products.findById(ci.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("product not found: " + ci.getProductId()));

            BigDecimal unit = p.getCurrentPrice();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(ci.getQuantity()));

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setSku(p.getSku());
            oi.setName(p.getName());
            oi.setUnitPrice(unit);
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotal(line);

            orderItems.add(oi);
            total = total.add(line);
        }

        for (var attempt = 1; attempt <= MAX_RETRY; attempt++){
            try {
                for (var oi : orderItems){
                    ProductStock s = stocks.findByProductId(oi.getProductId())
                        .orElseThrow(() -> new IllegalStateException("stock row missing for product " + oi.getProductId()));

                    long newQty = s.getQuantityOnHand() - oi.getQuantity();
                    if (newQty < 0) {
                        throw new IllegalStateException("insufficient stock for product " + oi.getProductId());
                    }
                    s.setQuantityOnHand(newQty);
                    stocks.save(s);  
                }

                Order order = new Order();
                order.setUserId(userId);
                order.setStatus(Order.Status.PAID); 
                order.setTotalAmount(total);
                order.setItems(orderItems);

                Order saved = orders.save(order);

                audit.log("CHECKOUT_SUCCESS", "ORDER", saved.getId(),
                        java.util.Map.of("total", total, "itemCount", orderItems.size()));
                cartService.clear(userId);

                return saved;

            } catch (OptimisticLockingFailureException e){
                if (attempt == MAX_RETRY) {
                    audit.log("CHECKOUT_FAILED", "ORDER", null,
                            java.util.Map.of("reason", "optimistic_lock_max_retry"));
                    throw new IllegalStateException("concurrency conflict, please retry");
                }
            }
        }

        throw new IllegalStateException("unexpected checkout failure");
    }
}