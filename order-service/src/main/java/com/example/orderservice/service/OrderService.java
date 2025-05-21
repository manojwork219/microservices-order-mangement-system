package com.example.orderservice.service;

import java.util.List;

import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.PlaceOrderRequest;
import com.example.orderservice.model.Order;

public interface OrderService {
    OrderResponse placeOrder(PlaceOrderRequest orderRequest);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
    OrderResponse mapToOrderResponse(Order order);
}
