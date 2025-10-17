package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stock_order.infrastructure.persistence.entity.OrderEntity;

public interface SpringDataOrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserId(Long userId);

    @Query("select o from OrderEntity o left join fetch o.items where o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") Long id);
}