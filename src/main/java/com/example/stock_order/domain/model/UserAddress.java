package com.example.stock_order.domain.model;

import java.time.Instant;

public class UserAddress {
    private Long id;
    private Long userId;
    private String title;
    private String recipientName;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;

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
