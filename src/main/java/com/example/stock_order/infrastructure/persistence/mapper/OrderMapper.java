package com.example.stock_order.infrastructure.persistence.mapper;

import java.util.stream.Collectors;

import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.model.OrderItem;
import com.example.stock_order.infrastructure.persistence.entity.OrderEntity;
import com.example.stock_order.infrastructure.persistence.entity.OrderItemEntity;

public final class OrderMapper {
    private OrderMapper(){}

    public static Order toDomain(OrderEntity e){
        if(e==null) return null;
        Order d = new Order();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setStatus(Order.Status.valueOf(e.getStatus().name()));
        d.setTotalAmount(e.getTotalAmount());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setItems(e.getItems().stream().map(OrderMapper::toDomain).collect(Collectors.toList()));

        d.setShippingName(e.getShippingName());
        d.setShippingLine1(e.getShippingLine1());
        d.setShippingLine2(e.getShippingLine2());
        d.setShippingCity(e.getShippingCity());
        d.setShippingState(e.getShippingState());
        d.setShippingPostalCode(e.getShippingPostalCode());
        d.setShippingCountry(e.getShippingCountry());
        d.setShippingPhone(e.getShippingPhone());
        return d;
    }

    public static Order toDomainNoItems(OrderEntity e){
        if(e==null) return null;
        Order d = new Order();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setStatus(Order.Status.valueOf(e.getStatus().name()));
        d.setTotalAmount(e.getTotalAmount());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setItems(java.util.List.of());

        d.setShippingName(e.getShippingName());
        d.setShippingLine1(e.getShippingLine1());
        d.setShippingLine2(e.getShippingLine2());
        d.setShippingCity(e.getShippingCity());
        d.setShippingState(e.getShippingState());
        d.setShippingPostalCode(e.getShippingPostalCode());
        d.setShippingCountry(e.getShippingCountry());
        d.setShippingPhone(e.getShippingPhone());
        return d;
    }

    public static OrderItem toDomain(OrderItemEntity e){
        if(e==null) return null;
        OrderItem d = new OrderItem();
        d.setId(e.getId());
        d.setOrderId(e.getOrder().getId());
        d.setProductId(e.getProductId());
        d.setSku(e.getSku());
        d.setName(e.getName());
        d.setUnitPrice(e.getUnitPrice());
        d.setQuantity(e.getQuantity());
        d.setLineTotal(e.getLineTotal());
        return d;
    }

    public static OrderEntity toEntity(Order d){
        OrderEntity e = new OrderEntity();
        e.setId(d.getId());
        e.setUserId(d.getUserId());
        e.setStatus(OrderEntity.Status.valueOf(d.getStatus().name()));
        e.setTotalAmount(d.getTotalAmount());

        e.setShippingName(d.getShippingName());
        e.setShippingLine1(d.getShippingLine1());
        e.setShippingLine2(d.getShippingLine2());
        e.setShippingCity(d.getShippingCity());
        e.setShippingState(d.getShippingState());
        e.setShippingPostalCode(d.getShippingPostalCode());
        e.setShippingCountry(d.getShippingCountry());
        e.setShippingPhone(d.getShippingPhone());
        return e;
    }
}
