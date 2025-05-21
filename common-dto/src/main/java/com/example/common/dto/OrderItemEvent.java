package com.example.common.dto;

public class OrderItemEvent {

    private String skuCode;
    private Integer quantity;
    private Double pricePerItem;

    public OrderItemEvent() {
    }

    public OrderItemEvent(String skuCode, Integer quantity, Double pricePerItem) {
        this.skuCode = skuCode;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPricePerItem() {
        return pricePerItem;
    }

    public void setPricePerItem(Double pricePerItem) {
        this.pricePerItem = pricePerItem;
    }
}
