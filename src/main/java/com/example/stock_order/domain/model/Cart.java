package com.example.stock_order.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private Long id;
    private Long userId;
    private List<CartItem> items = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public List<CartItem> getItems() { return items; } public void setItems(List<CartItem> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}