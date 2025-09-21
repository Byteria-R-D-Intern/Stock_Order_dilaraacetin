package com.example.stock_order.domain.model;


import java.time.Instant;

public class ProductStock {
    private Long productId;          
    private Long quantityOnHand;     
    private Long version;            
    private Instant createdAt;
    private Instant updatedAt;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(Long quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}