package com.example.stock_order.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders", indexes = @Index(name="ix_orders_user", columnList="user_id"))
public class OrderEntity {

    public enum Status { PENDING, PAID, FAILED, CANCELLED, SHIPPED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Status status;

    @Column(name="total_amount", nullable=false, precision=14, scale=2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval = true, fetch=FetchType.LAZY)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;
    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @PrePersist void onCreate(){ Instant now=Instant.now(); if(createdAt==null)createdAt=now; updatedAt=now; }
    @PreUpdate  void onUpdate(){ updatedAt=Instant.now(); }

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public Status getStatus(){return status;} public void setStatus(Status status){this.status=status;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal totalAmount){this.totalAmount=totalAmount;}
    public List<OrderItemEntity> getItems(){return items;} public void setItems(List<OrderItemEntity> items){this.items=items;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant updatedAt){this.updatedAt=updatedAt;}
}