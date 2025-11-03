package com.example.stock_order.application;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.config.JwtService;
import com.example.stock_order.domain.ports.repository.UserRepository;
import com.example.stock_order.infrastructure.persistence.entity.AuditLogEntity;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataAuditLogJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final SpringDataAuditLogJpaRepository auditRepo;
    private final UserRepository users;
    private final JwtService jwtService;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    private final ObjectMapper om = new ObjectMapper();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, Map<String, Object> details) {
        var actor = resolveActor(); 

        String detailsJson = null;
        try {
            if (details != null && !details.isEmpty()) {
                detailsJson = om.writeValueAsString(details);
            }
        } catch (Exception ignored) {}

        var e = new AuditLogEntity();
        e.setActorUserId(actor.userId);
        e.setActorEmail(actor.email);
        e.setAction(action);
        e.setEntityType(entityType);
        e.setEntityId(entityId);
        e.setDetails(detailsJson);

        auditRepo.save(e);
    }
    @Transactional(readOnly = true)
    public java.util.List<AuditLogEntity> findAll() {
        return auditRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWithActor(Long actorUserId, String actorEmail, String action, String entityType, Long entityId, Map<String, Object> details) {
        String detailsJson = null;
        try {
            if (details != null && !details.isEmpty()) {
                detailsJson = om.writeValueAsString(details);
            }
        } catch (Exception ignored) {}

        var e = new AuditLogEntity();
        e.setActorUserId(actorUserId);
        e.setActorEmail(actorEmail);
        e.setAction(action);
        e.setEntityType(entityType);
        e.setEntityId(entityId);
        e.setDetails(detailsJson);

        auditRepo.save(e);
    }


    private static final class Actor {
        final Long userId;
        final String email;
        Actor(Long userId, String email) { this.userId = userId; this.email = email; }
        static Actor anonymous() { return new Actor(null, null); }
    }

    private Actor resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = safeName(auth);
            if (email != null && !email.equalsIgnoreCase("anonymousUser")) {
                Long userId = users.findByEmail(email).map(u -> u.getId()).orElse(null);
                return new Actor(userId, email);
            }
        }

        HttpServletRequest req = requestProvider.getIfAvailable();
        if (req != null) {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    String emailFromJwt = jwtService.extractEmail(token);
                    String userIdStr   = jwtService.extractUserId(token);
                    Long userId = null;
                    try { userId = (userIdStr != null) ? Long.valueOf(userIdStr) : null; } catch (NumberFormatException ignore) {}
                    return new Actor(userId, emailFromJwt);
                } catch (Exception ignore) {
                }
            }
        }

        return Actor.anonymous();
    }

    private String safeName(Authentication auth) {
        try {
            return auth.getName();
        } catch (Exception e) {
            return null;
        }
    }
}
