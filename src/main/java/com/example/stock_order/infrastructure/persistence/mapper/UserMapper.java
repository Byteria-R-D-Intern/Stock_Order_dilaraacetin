package com.example.stock_order.infrastructure.persistence.mapper;

import com.example.stock_order.domain.model.User;
import com.example.stock_order.infrastructure.persistence.entity.UserEntity;

public final class UserMapper {
    private UserMapper() {}

    public static User toDomain(UserEntity e) {
        if (e == null) return null;
        User d = new User();
        d.setId(e.getId());
        d.setEmail(e.getEmail());
        d.setPasswordHash(e.getPasswordHash());
        d.setRole(User.Role.valueOf(e.getRole().name()));
        d.setActive(e.isActive());
        d.setFailedLoginCount(e.getFailedLoginCount());
        d.setLockedUntil(e.getLockedUntil());
        d.setLastLoginAt(e.getLastLoginAt());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    public static UserEntity toEntity(User d) {
        if (d == null) return null;
        UserEntity e = new UserEntity();
        e.setId(d.getId());
        e.setEmail(d.getEmail());
        e.setPasswordHash(d.getPasswordHash());
        e.setRole(UserEntity.Role.valueOf(d.getRole().name()));
        e.setActive(d.isActive());
        e.setFailedLoginCount(d.getFailedLoginCount());
        e.setLockedUntil(d.getLockedUntil());
        e.setLastLoginAt(d.getLastLoginAt());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
