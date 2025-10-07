package com.example.stock_order.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "ix_cart_items_cart", columnList = "cart_id")
})
public class CartItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cart_id", nullable=false)
    private CartEntity cart;

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

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public CartEntity getCart(){return cart;} public void setCart(CartEntity cart){this.cart=cart;}
    public Long getProductId(){return productId;} public void setProductId(Long productId){this.productId=productId;}
    public String getSku(){return sku;} public void setSku(String sku){this.sku=sku;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal unitPrice){this.unitPrice=unitPrice;}
    public Long getQuantity(){return quantity;} public void setQuantity(Long quantity){this.quantity=quantity;}
}