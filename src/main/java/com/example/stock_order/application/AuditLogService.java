package com.example.stock_order.application;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.ports.repository.UserRepository;
import com.example.stock_order.infrastructure.persistence.entity.AuditLogEntity;
import com.example.stock_order.infrastructure.persistence.springdata.SpringDataAuditLogJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final SpringDataAuditLogJpaRepository auditRepo;
    private final UserRepository users;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, Map<String, Object> details) {
        Long actorUserId = null;
        String actorEmail = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            actorEmail = auth.getName(); 
            if (actorEmail != null) {
                actorUserId = users.findByEmail(actorEmail).map(u -> u.getId()).orElse(null);
            }
        }

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
}
