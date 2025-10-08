package com.example.stock_order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private Long userId;
    private Status status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItem> items = new ArrayList<>();

    public enum Status { PENDING, PAID, FAILED, CANCELLED, SHIPPED }

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public Status getStatus(){return status;} public void setStatus(Status status){this.status=status;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal totalAmount){this.totalAmount=totalAmount;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant updatedAt){this.updatedAt=updatedAt;}
    public List<OrderItem> getItems(){return items;} public void setItems(List<OrderItem> items){this.items=items;}
}