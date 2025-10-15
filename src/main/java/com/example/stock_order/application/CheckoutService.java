package com.example.stock_order.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.model.Cart;
import com.example.stock_order.domain.model.CartItem;
import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.model.OrderItem;
import com.example.stock_order.domain.model.Product;
import com.example.stock_order.domain.model.ProductStock;
import com.example.stock_order.domain.model.User;
import com.example.stock_order.domain.ports.repository.CartRepository;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;
import com.example.stock_order.domain.ports.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final int MAX_RETRY = 3;

    private final UserRepository users;
    private final ProductRepository products;
    private final ProductStockRepository stocks;
    private final CartRepository carts;
    private final OrderRepository orders;
    private final PaymentService payment;
    private final AuditLogService audit;

    @Transactional
    public Order checkout(String paymentToken) {
        Long userId = currentUserIdOrThrow();

        Cart cart = carts.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("cart is empty"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> snapshotItems = new ArrayList<>();

        for (CartItem ci : cart.getItems()) {
            Product p = products.findById(ci.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("product not found: " + ci.getProductId()));
            if (p.getStatus() != Product.Status.ACTIVE) {
                throw new IllegalArgumentException("product is not active: " + p.getId());
            }
            BigDecimal unit = p.getCurrentPrice();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(line);

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setSku(p.getSku());
            oi.setName(p.getName());
            oi.setUnitPrice(unit);
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotal(line);
            snapshotItems.add(oi);
        }

        payment.charge(paymentToken, total, "TRY");

        int attempt = 0;
        while (true) {
            try {
                for (OrderItem oi : snapshotItems) {
                    ProductStock s = stocks.findByProductId(oi.getProductId())
                            .orElseThrow(() -> new IllegalStateException("stock row missing: " + oi.getProductId()));

                    long newQty = s.getQuantityOnHand() - oi.getQuantity();
                    if (newQty < 0) {
                        throw new IllegalArgumentException("insufficient stock for product " + oi.getProductId());
                    }
                    s.setQuantityOnHand(newQty);
                    stocks.save(s);
                }

                Order order = new Order();
                order.setUserId(userId);
                order.setStatus(Order.Status.PAID);
                order.setTotalAmount(total);
                order.setCreatedAt(Instant.now());
                order.setUpdatedAt(Instant.now());
                order.setItems(snapshotItems);

                Order saved = orders.save(order);

                cart.getItems().clear();
                carts.save(cart);

                audit.log("CHECKOUT_SUCCESS", "ORDER", saved.getId(),
                        java.util.Map.of("total", total, "itemCount", snapshotItems.size()));

                return saved;

            } catch (ObjectOptimisticLockingFailureException ole) {
                attempt++;
                if (attempt >= MAX_RETRY) {
                    audit.log("CHECKOUT_CONCURRENCY_FAIL", "ORDER", null,
                            java.util.Map.of("reason", "optimistic_lock", "attempts", attempt));
                    throw new IllegalArgumentException("concurrency conflict, please retry");
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Order> listMyOrders() {
        Long userId = currentUserIdOrThrow();
        List<Order> list = new ArrayList<>(orders.findByUserId(userId));
        list.sort(
            java.util.Comparator
                .comparing(Order::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                .reversed()
        );
        return list;
    }

    @Transactional(readOnly = true)
    public Order getMyOrder(Long orderId) {
        Long userId = currentUserIdOrThrow();
        Order o = orders.findById(orderId).orElseThrow(() ->
                new IllegalArgumentException("order not found"));
        if (!o.getUserId().equals(userId)) {
            throw new org.springframework.security.authorization.AuthorizationDeniedException("Access Denied");
        }
        return o;
    }

    private Long currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("unauthenticated");
        }
        String email = auth.getName();
        User u = users.findByEmail(email).orElseThrow(() ->
                new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("user not found"));
        return u.getId();
    }
}
