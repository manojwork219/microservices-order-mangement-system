package com.example.orderservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String orderNumber;
    private String customerName;
    private String paymentMode;
    private String deliveryAddress;
    private String orderStatus;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;
    private double totalPrice;
}
