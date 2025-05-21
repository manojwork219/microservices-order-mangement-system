package com.example.common.dto;

import java.util.List;

public class OrderPlacedEvent {

    private String orderNumber;
    private List<OrderItemEvent> orderItems;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(String orderNumber, List<OrderItemEvent> orderItems) {
        this.orderNumber = orderNumber;
        this.orderItems = orderItems;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<OrderItemEvent> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemEvent> orderItems) {
        this.orderItems = orderItems;
    }
}
