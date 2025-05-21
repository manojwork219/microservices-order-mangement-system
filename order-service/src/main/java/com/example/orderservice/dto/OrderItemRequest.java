package com.example.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotBlank(message = "SKU code is required")
    private String skuCode;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
