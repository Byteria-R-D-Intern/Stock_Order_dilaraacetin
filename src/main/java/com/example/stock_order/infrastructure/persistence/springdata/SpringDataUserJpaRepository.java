package com.example.stock_order.infrastructure.persistence.springdata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.UserEntity;

public interface SpringDataUserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
