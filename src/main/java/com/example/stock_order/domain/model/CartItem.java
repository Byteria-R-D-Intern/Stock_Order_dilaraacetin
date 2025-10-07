package com.example.stock_order.domain.model;

import java.math.BigDecimal;

public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal unitPrice;   
    private Long quantity;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getCartId() { return cartId; } public void setCartId(Long cartId) { this.cartId = cartId; }
    public Long getProductId() { return productId; } public void setProductId(Long productId) { this.productId = productId; }
    public String getSku() { return sku; } public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public BigDecimal getUnitPrice() { return unitPrice; } public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Long getQuantity() { return quantity; } public void setQuantity(Long quantity) { this.quantity = quantity; }
}