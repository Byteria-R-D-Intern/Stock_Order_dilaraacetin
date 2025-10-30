package com.example.stock_order.domain.ports.repository;

import java.util.List;
import java.util.Optional;

import com.example.stock_order.domain.model.User;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);   
    List<User> findAll(); 
}
