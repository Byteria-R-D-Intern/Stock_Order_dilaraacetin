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

    @Column(name="shipping_name", length=128)
    private String shippingName;
    @Column(name="shipping_line1", length=255)
    private String shippingLine1;
    @Column(name="shipping_line2", length=255)
    private String shippingLine2;
    @Column(name="shipping_city", length=128)
    private String shippingCity;
    @Column(name="shipping_state", length=128)
    private String shippingState;
    @Column(name="shipping_postal_code", length=32)
    private String shippingPostalCode;
    @Column(name="shipping_country", length=64)
    private String shippingCountry;
    @Column(name="shipping_phone", length=32)
    private String shippingPhone;
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
    public String getShippingName() { return shippingName; } public void setShippingName(String shippingName) { this.shippingName = shippingName; }
    public String getShippingLine1() { return shippingLine1; } public void setShippingLine1(String shippingLine1) { this.shippingLine1 = shippingLine1; }
    public String getShippingLine2() { return shippingLine2; } public void setShippingLine2(String shippingLine2) { this.shippingLine2 = shippingLine2; }
    public String getShippingCity() { return shippingCity; } public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
    public String getShippingState() { return shippingState; } public void setShippingState(String shippingState) { this.shippingState = shippingState; }
    public String getShippingPostalCode() { return shippingPostalCode; } public void setShippingPostalCode(String shippingPostalCode) { this.shippingPostalCode = shippingPostalCode; }
    public String getShippingCountry() { return shippingCountry; } public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
    public String getShippingPhone() { return shippingPhone; } public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }
}

