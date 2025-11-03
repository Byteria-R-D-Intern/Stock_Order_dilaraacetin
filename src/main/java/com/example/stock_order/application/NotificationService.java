package com.example.stock_order.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_order.domain.model.Notification;
import com.example.stock_order.infrastructure.persistence.entity.NotificationEntity;
import com.example.stock_order.infrastructure.persistence.springdata.NotificationJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationJpaRepository repo;
    private final AuditLogService audit;

    public List<NotificationEntity> listAllForUser(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(Long userId) {
        return repo.countByUserIdAndReadFalse(userId);
    }

    public void markRead(Long userId, Long notificationId) {
        var e = repo.findById(notificationId).orElseThrow();
        if (!e.getUserId().equals(userId)) throw new IllegalArgumentException("not_owner");
        if (!e.isRead()) {
            e.setRead(true);
            repo.save(e);
            audit.log("NOTIFICATION_READ", "NOTIFICATION", e.getId(),
                    java.util.Map.of("userId", userId));
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        var unread = repo.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        if (unread.isEmpty()) {
            audit.log("NOTIFICATION_MARK_ALL_READ", "USER", userId, java.util.Map.of("count", 0));
            return;
        }
        unread.forEach(n -> n.setRead(true));
        repo.saveAll(unread);
        audit.log("NOTIFICATION_MARK_ALL_READ", "USER", userId, java.util.Map.of("count", unread.size()));
    }

    public void delete(Long userId, Long notificationId) {
        var e = repo.findById(notificationId).orElseThrow();
        if (!e.getUserId().equals(userId)) throw new IllegalArgumentException("not_owner");
        repo.deleteById(notificationId);
        audit.log("NOTIFICATION_DELETED", "NOTIFICATION", notificationId,
                java.util.Map.of("userId", userId));
    }

    public NotificationEntity create(Long userId, Notification type, String title, String message) {
        var e = new NotificationEntity();
        e.setUserId(userId);
        e.setType(type);
        e.setTitle(title);
        e.setMessage(message);
        var saved = repo.save(e);
        audit.log("NOTIFICATION_CREATED", "NOTIFICATION", saved.getId(),
                java.util.Map.of("userId", userId, "type", type.name()));
        return saved;
    }

    public void notifyOrderStatus(Long userId, Long orderId, String status) {
        create(userId, Notification.ORDER,
                "Order #" + orderId + " status: " + status,
                "Your order #" + orderId + " is now " + status + ".");
    }

    public void notifyPayment(Long userId, String msg, boolean success) {
        create(userId, Notification.PAYMENT,
                success ? "Payment successful" : "Payment failed",
                msg);
    }

    public void notifyProduct(Long userId, String title, String msg) {
        create(userId, Notification.PRODUCT, title, msg);
    }

    public void notifyAccount(Long userId, String title, String msg) {
        create(userId, Notification.ACCOUNT, title, msg);
    }
}
