package com.example.stock_order.infrastructure.persistence.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name="order_items", indexes = @Index(name="ix_order_items_order", columnList="order_id"))
public class OrderItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id", nullable=false)
    private OrderEntity order;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(nullable=false, length=64)
    private String sku;

    @Column(nullable=false, length=255)
    private String name;

    @Column(name="unit_price", nullable=false, precision=12, scale=2)
    private BigDecimal unitPrice;

    @Column(nullable=false)
    private Long quantity;

    @Column(name="line_total", nullable=false, precision=14, scale=2)
    private BigDecimal lineTotal;

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public OrderEntity getOrder(){return order;} public void setOrder(OrderEntity order){this.order=order;}
    public Long getProductId(){return productId;} public void setProductId(Long productId){this.productId=productId;}
    public String getSku(){return sku;} public void setSku(String sku){this.sku=sku;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal unitPrice){this.unitPrice=unitPrice;}
    public Long getQuantity(){return quantity;} public void setQuantity(Long quantity){this.quantity=quantity;}
    public BigDecimal getLineTotal(){return lineTotal;} public void setLineTotal(BigDecimal lineTotal){this.lineTotal=lineTotal;}
}