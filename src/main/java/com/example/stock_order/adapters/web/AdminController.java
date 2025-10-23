package com.example.stock_order.adapters.web;


import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.user.UpdateUserRoleRequest;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.domain.ports.repository.UserRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final UserRepository users;
    private final AuditLogService audit;

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                        @RequestBody @Valid UpdateUserRoleRequest req) {
        return users.findById(id)
                .map(u -> {
                    var oldRole = u.getRole().name();
                    u.setRole(req.role());
                    var saved = users.save(u);

                    audit.log("USER_ROLE_CHANGED", "USER", saved.getId(),
                              Map.of("oldRole", oldRole, "newRole", saved.getRole().name(), "email", saved.getEmail()));

                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        return users.findById(id).map(u -> {
            u.setActive(true);
            users.save(u);
            audit.log("USER_ACTIVATED", "USER", u.getId(), Map.of("email", u.getEmail()));
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        return users.findById(id).map(u -> {
            u.setActive(false);
            users.save(u);
            audit.log("USER_DEACTIVATED", "USER", u.getId(), Map.of("email", u.getEmail()));
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}