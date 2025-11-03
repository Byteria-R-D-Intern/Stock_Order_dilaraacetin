package com.example.stock_order.adapters.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.notification.NotificationResponse;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.application.NotificationService;
import com.example.stock_order.domain.model.User;
import com.example.stock_order.domain.ports.repository.UserRepository;
import com.example.stock_order.infrastructure.persistence.entity.AuditLogEntity;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class NotificationsController {

    private final NotificationService notifications;
    private final UserRepository users;
    private final AuditLogService auditLogService;

    private Long currentUserId(Authentication auth) {
        String email = auth.getName();
        return users.findByEmail(email).map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("kullanıcı bulunamadı"));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(Authentication auth) {
        Long uid = currentUserId(auth);
        var list = notifications.listAllForUser(uid).stream()
                .map(NotificationResponse::of)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication auth) {
        Long uid = currentUserId(auth);
        return ResponseEntity.ok(Map.of("unread", notifications.unreadCount(uid)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(Authentication auth, @PathVariable Long id) {
        notifications.markRead(currentUserId(auth), id);
        return ResponseEntity.ok().build(); 
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication auth) {
        notifications.markAllRead(currentUserId(auth));
        return ResponseEntity.ok().build(); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        notifications.delete(currentUserId(auth), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogEntity>> allAuditLogs() {
        return ResponseEntity.ok(auditLogService.findAll());
    }

}
