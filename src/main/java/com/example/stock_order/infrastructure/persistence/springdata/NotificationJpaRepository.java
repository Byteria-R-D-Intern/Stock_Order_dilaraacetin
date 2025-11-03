package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.NotificationEntity;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    List<NotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
}
