package com.example.stock_order.application;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.domain.ports.repository.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private final OrderRepository orders;
    private final ProductStockRepository stocks;
    private final AuditLogService audit;

    @Transactional
    public void changeStatus(Long id, Order.Status status) {
        var order = orders.findByIdWithItems(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("order not found"));

        var prev = order.getStatus();

        if (prev != Order.Status.CANCELLED && status == Order.Status.CANCELLED) {
            for (var it : order.getItems()) {
                var s = stocks.findByProductId(it.getProductId())
                        .orElseThrow(() -> new IllegalStateException("stock row missing: " + it.getProductId()));
                s.setQuantityOnHand(s.getQuantityOnHand() + it.getQuantity());
                stocks.save(s);
            }
        }

        order.setStatus(status);
        orders.save(order);

        audit.log("ORDER_STATUS_CHANGED", "ORDER", order.getId(),
                Map.of("previous", prev.name(), "current", status.name()));
    }
}