package com.example.stock_order.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.adapters.web.exception.NotFoundException;
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
import com.example.stock_order.domain.ports.repository.UserAddressRepository;
import com.example.stock_order.domain.ports.repository.UserRepository;
import com.example.stock_order.infrastructure.persistence.springdata.SavedPaymentMethodJpaRepository;

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

    private final SavedPaymentMethodJpaRepository savedPaymentMethodRepo;
    private final UserAddressRepository addresses; 

    @Transactional
    public Order checkoutWithToken(String paymentToken, Long shippingAddressId) {
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("payment token missing");
        }
        if (shippingAddressId == null) {
            throw new IllegalArgumentException("shipping address missing");
        }
        return doCheckout(paymentToken, null, shippingAddressId);
    }

    @Transactional
    public Order checkoutWithSaved(Long savedPaymentMethodId, Long shippingAddressId) {
        if (savedPaymentMethodId == null) {
            throw new IllegalArgumentException("saved payment method missing");
        }
        if (shippingAddressId == null) {
            throw new IllegalArgumentException("shipping address missing");
        }
        return doCheckout(null, savedPaymentMethodId, shippingAddressId);
    }

    private Order doCheckout(String paymentToken, Long savedPaymentMethodId, Long shippingAddressId) {
        Long userId = currentUserIdOrThrow();

        var addr = addresses.findByIdAndUserId(shippingAddressId, userId)
                .orElseThrow(() -> new NotFoundException("address not found"));

        Cart cart = carts.findByUserId(userId).orElseThrow(() -> new NotFoundException("sepet boş"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new NotFoundException("sepet boş");
        }
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> snapshotItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Product p = products.findById(ci.getProductId())
                    .orElseThrow(() -> new NotFoundException("ürün bulunamadı: " + ci.getProductId()));
            if (p.getStatus() != Product.Status.ACTIVE) {
                throw new IllegalArgumentException("ürün aktif değil: " + p.getId());
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

        if (savedPaymentMethodId != null) {
            var spm = savedPaymentMethodRepo
                .findByIdAndUserIdAndActiveTrue(savedPaymentMethodId, userId)
                .orElseThrow(() -> new NotFoundException("payment method not found"));

            payment.chargeSaved(spm.getToken(), total, "TRY", spm.getLast4(), spm.getBrand());

        } else {
            payment.chargeOneTime(paymentToken, total, "TRY");
        }

        int attempt = 0;
        while (true) {
            try {
                for (OrderItem oi : snapshotItems) {
                    ProductStock s = stocks.findByProductId(oi.getProductId())
                            .orElseThrow(() -> new NotFoundException("stok bilgisi bulunamadı: " + oi.getProductId()));
                    long newQty = s.getQuantityOnHand() - oi.getQuantity();
                    if (newQty < 0) throw new IllegalArgumentException("ürün için yetersiz stok miktarı " + oi.getProductId());
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

                order.setShippingName(addr.getRecipientName());
                order.setShippingLine1(addr.getLine1());
                order.setShippingLine2(addr.getLine2());
                order.setShippingCity(addr.getCity());
                order.setShippingState(addr.getState());
                order.setShippingPostalCode(addr.getPostalCode());
                order.setShippingCountry(addr.getCountry());
                order.setShippingPhone(addr.getPhone());

                Order saved = orders.save(order);

                cart.getItems().clear();
                carts.save(cart);

                audit.log("CHECKOUT_SUCCESS", "ORDER", saved.getId(),
                        java.util.Map.of("total", total, "itemCount", snapshotItems.size(), "addrId", shippingAddressId));
                return saved;

            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException ole) {
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
        java.util.List<Order> list = new java.util.ArrayList<>(orders.findByUserId(userId));
        list.sort(java.util.Comparator.comparing(Order::getCreatedAt,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed());
        return list;
    }

    @Transactional(readOnly = true)
    public Order getMyOrder(Long orderId) {
        Long userId = currentUserIdOrThrow();
        Order o = orders.findById(orderId).orElseThrow(() -> new NotFoundException("sipariş bulunamadı"));
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
        User u = users.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("kullaıcı bulunamadı"));
        return u.getId();
    }
}
