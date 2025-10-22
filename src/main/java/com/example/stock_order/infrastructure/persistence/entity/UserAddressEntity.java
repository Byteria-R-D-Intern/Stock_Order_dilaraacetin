package com.example.stock_order.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_addresses", indexes = { @Index(name = "ix_addr_user", columnList = "user_id") })
public class UserAddressEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable=false, length=64)
    private String title;

    @Column(name="recipient_name", nullable=false, length=128)
    private String recipientName;

    @Column(name="line1", nullable=false, length=255)
    private String line1;

    @Column(name="line2", length=255)
    private String line2;

    @Column(nullable=false, length=128)
    private String city;

    @Column(length=128)
    private String state;

    @Column(name="postal_code", length=32)
    private String postalCode;

    @Column(nullable=false, length=64)
    private String country;

    @Column(length=32)
    private String phone;

    @Column(name="is_default", nullable=false)
    private boolean isDefault;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @PrePersist void onCreate(){ Instant now=Instant.now(); if(createdAt==null)createdAt=now; updatedAt=now; }
    @PreUpdate  void onUpdate(){ updatedAt=Instant.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getRecipientName() { return recipientName; } public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getLine1() { return line1; } public void setLine1(String line1) { this.line1 = line1; }
    public String getLine2() { return line2; } public void setLine2(String line2) { this.line2 = line2; }
    public String getCity() { return city; } public void setCity(String city) { this.city = city; }
    public String getState() { return state; } public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; } public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; } public void setCountry(String country) { this.country = country; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public boolean isDefault() { return isDefault; } public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
