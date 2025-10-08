package com.example.stock_order.domain.model;

import java.math.BigDecimal;

public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal unitPrice;
    private Long quantity;
    private BigDecimal lineTotal;

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getOrderId(){return orderId;} public void setOrderId(Long orderId){this.orderId=orderId;}
    public Long getProductId(){return productId;} public void setProductId(Long productId){this.productId=productId;}
    public String getSku(){return sku;} public void setSku(String sku){this.sku=sku;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal unitPrice){this.unitPrice=unitPrice;}
    public Long getQuantity(){return quantity;} public void setQuantity(Long quantity){this.quantity=quantity;}
    public BigDecimal getLineTotal(){return lineTotal;} public void setLineTotal(BigDecimal lineTotal){this.lineTotal=lineTotal;}
}