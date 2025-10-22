package com.example.stock_order.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stock_order.domain.model.UserAddress;
import com.example.stock_order.domain.ports.repository.UserAddressRepository;
import com.example.stock_order.infrastructure.persistence.mapper.UserAddressMapper;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataUserAddressJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserAddressRepositoryJpaAdapter implements UserAddressRepository {

    private final SpringDataUserAddressJpaRepository jpa;

    @Override public List<UserAddress> findByUserId(Long userId){
        return jpa.findByUserId(userId).stream().map(UserAddressMapper::toDomain).toList();
    }

    @Override public Optional<UserAddress> findByIdAndUserId(Long id, Long userId){
        return jpa.findByIdAndUserId(id, userId).map(UserAddressMapper::toDomain);
    }

    @Override public UserAddress save(UserAddress address){
        var saved = jpa.save(UserAddressMapper.toEntity(address));
        return UserAddressMapper.toDomain(saved);
    }

    @Override public void deleteByIdAndUserId(Long id, Long userId){
        jpa.deleteByIdAndUserId(id, userId);
    }

    @Override public void clearDefaultForUser(Long userId){
        var list = jpa.findByUserId(userId);
        boolean changed = false;
        for (var e : list) {
            if (e.isDefault()) { e.setDefault(false); changed = true; }
        }
        if (changed) jpa.saveAll(list);
    }
}
