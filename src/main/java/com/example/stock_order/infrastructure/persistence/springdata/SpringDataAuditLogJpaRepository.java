package com.example.stock_order.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_order.infrastructure.persistence.entity.AuditLogEntity;

public interface SpringDataAuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {}
