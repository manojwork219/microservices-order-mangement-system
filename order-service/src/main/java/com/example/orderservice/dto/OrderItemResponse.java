// com.example.orderservice.dto.OrderItemResponse
package com.example.orderservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String skuCode;
    private int quantity;
    private double pricePerItem;
}
