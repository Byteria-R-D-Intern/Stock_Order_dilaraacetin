package com.example.stock_order.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stock_order.domain.model.User;
import com.example.stock_order.domain.ports.repository.UserRepository;
import com.example.stock_order.infrastructure.persistence.mapper.UserMapper;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataUserJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryJpaAdapter implements UserRepository {

    private final SpringDataUserJpaRepository jpa;

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var saved = jpa.save(UserMapper.toEntity(user));
        return UserMapper.toDomain(saved);
    }
}