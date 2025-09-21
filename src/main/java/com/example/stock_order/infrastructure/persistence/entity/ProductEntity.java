package com.example.stock_order.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "products",
    indexes = { @Index(name = "ux_products_sku", columnList = "sku", unique = true) }
)
public class ProductEntity {

    public enum Status { ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=64)
    private String sku;

    @Column(nullable=false, length=255)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name="current_price", nullable=false, precision=12, scale=2)
    private BigDecimal currentPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Status status = Status.ACTIVE;

    @Version
    private Long version;

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
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; } public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public BigDecimal getCurrentPrice() { return currentPrice; } public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public Status getStatus() { return status; } public void setStatus(Status status) { this.status = status; }
    public Long getVersion() { return version; } public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}