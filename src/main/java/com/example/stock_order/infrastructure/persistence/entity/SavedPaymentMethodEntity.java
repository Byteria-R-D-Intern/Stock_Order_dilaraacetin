package com.example.stock_order.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "saved_payment_methods", indexes = {
    @Index(name = "ix_spm_user", columnList = "user_id")
})
public class SavedPaymentMethodEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable=false, unique=true, length=128)
    private String token;

    @Column(nullable=false, length=4)
    private String last4;

    @Column(nullable=false, length=20)
    private String brand;

    @Column(name="expiry_month", nullable=false, length=2)
    private String expiryMonth;

    @Column(name="expiry_year", nullable=false, length=2)
    private String expiryYear;

    @Column(nullable=false)
    private boolean active = true;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // getters & setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getToken() { return token; }
    public String getLast4() { return last4; }
    public String getBrand() { return brand; }
    public String getExpiryMonth() { return expiryMonth; }
    public String getExpiryYear() { return expiryYear; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setToken(String token) { this.token = token; }
    public void setLast4(String last4) { this.last4 = last4; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }
    public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
