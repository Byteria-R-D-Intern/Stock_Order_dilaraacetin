package com.example.stock_order.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stock_order.domain.model.Order;
import com.example.stock_order.domain.ports.repository.OrderRepository;
import com.example.stock_order.infrastructure.persistence.entity.OrderEntity;
import com.example.stock_order.infrastructure.persistence.entity.OrderItemEntity;
import com.example.stock_order.infrastructure.persistence.mapper.OrderMapper;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataOrderJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryJpaAdapter implements OrderRepository {

    private final SpringDataOrderJpaRepository jpa;

    @Override
    public Order save(Order order){
        OrderEntity e = OrderMapper.toEntity(order);
        e.getItems().clear();
        if(order.getItems()!=null){
            for(var di: order.getItems()){
                OrderItemEntity oi = new OrderItemEntity();
                oi.setOrder(e);
                oi.setProductId(di.getProductId());
                oi.setSku(di.getSku());
                oi.setName(di.getName());
                oi.setUnitPrice(di.getUnitPrice());
                oi.setQuantity(di.getQuantity());
                oi.setLineTotal(di.getLineTotal());
                e.getItems().add(oi);
            }
        }
        return OrderMapper.toDomain(jpa.save(e));
    }

    @Override public Optional<Order> findById(Long id){
        return jpa.findById(id).map(OrderMapper::toDomain);
    }

    @Override public List<Order> findByUserId(Long userId){
        return jpa.findByUserId(userId).stream().map(OrderMapper::toDomain).toList();
    }

    @Override public Optional<Order> findByIdWithItems(Long id){
        return jpa.findByIdWithItems(id).map(OrderMapper::toDomain);
    }
    @Override
    public List<Order> findAll() {
        return jpa.findAll().stream()
                .map(OrderMapper::toDomainNoItems) 
                .toList();
    }
}
