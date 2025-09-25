package com.example.stock_order.adapters.web;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.user.UpdateUserRoleRequest;
import com.example.stock_order.domain.ports.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository users;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                        @RequestBody @Valid UpdateUserRoleRequest req) {
        return users.findById(id)
                .map(u -> {
                    u.setRole(req.role());
                    users.save(u);
                    return ResponseEntity.ok().build();         
                })
                .orElse(ResponseEntity.notFound().build());     
    }
}