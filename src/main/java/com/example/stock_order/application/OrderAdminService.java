package com.example.stock_order.application;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.adapters.web.exception.NotFoundException;
import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.model.Product;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.domain.ports.repository.ProductRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private final OrderRepository orders;
    private final ProductStockRepository stocks;
    private final AuditLogService audit;
    private final NotificationService notifications;
    private final ProductRepository products;

    @Transactional
    public void changeStatus(Long id, Order.Status status) {
        var order = orders.findByIdWithItems(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        var prev = order.getStatus();
        if (prev == status) {
            audit.log("ORDER_STATUS_NOOP", "ORDER", order.getId(),
                    Map.of("previous", prev != null ? prev.name() : null,
                           "current", status.name()));
            return;
        }

        if (prev == Order.Status.CANCELLED && status != Order.Status.CANCELLED) {
            throw new IllegalArgumentException("order_already_cancelled");
        }

        if (prev != Order.Status.CANCELLED && status == Order.Status.CANCELLED) {
            for (var it : order.getItems()) {
                var s = stocks.findByProductId(it.getProductId())
                        .orElseThrow(() -> new IllegalStateException("stock row missing: " + it.getProductId()));

                long oldQty = s.getQuantityOnHand();
                long newQty = oldQty + it.getQuantity();
                s.setQuantityOnHand(newQty);
                stocks.save(s);

                products.findById(it.getProductId()).ifPresent(p -> {
                    if (p.getStatus() == Product.Status.INACTIVE && oldQty == 0 && newQty > 0) {
                        p.setStatus(Product.Status.ACTIVE);
                        products.save(p);

                        audit.log(
                                "PRODUCT_REACTIVATED_AFTER_RETURN",
                                "PRODUCT",
                                p.getId(),
                                Map.of("newQty", newQty)
                        );
                    }
                });
            }
        }

        order.setStatus(status);
        orders.save(order);

        audit.log("ORDER_STATUS_CHANGED", "ORDER", order.getId(),
                Map.of("previous", prev != null ? prev.name() : null,
                       "current", status.name()));

        try {
            if (order.getUserId() != null) {
                notifications.notifyOrderStatus(order.getUserId(), order.getId(), status.name());
            }
        } catch (Exception ignore) { }
    }
}
