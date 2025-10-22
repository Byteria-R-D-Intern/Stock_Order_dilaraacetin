package com.example.stock_order.domain.ports.repository;

import java.util.List;
import java.util.Optional;
import com.example.stock_order.domain.model.UserAddress;

public interface UserAddressRepository {
    List<UserAddress> findByUserId(Long userId);
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
    UserAddress save(UserAddress address);
    void deleteByIdAndUserId(Long id, Long userId);
    void clearDefaultForUser(Long userId);
}
