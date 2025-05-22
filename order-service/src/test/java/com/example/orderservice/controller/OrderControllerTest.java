package com.example.orderservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.example.orderservice.dto.OrderItemResponse;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.PlaceOrderRequest;
import com.example.orderservice.exception.OrderNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.example.orderservice.service.OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class DummyConfig {
        @Bean(name = "dummyRestTemplate")
        public RestTemplate dummyRestTemplate() {
            return new RestTemplate();
        }
    }

    @Test
    void getAllOrders_success() throws Exception {
        OrderResponse order = buildSampleOrderResponse();

        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].customerName").value("John Doe"));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getOrderById_success() throws Exception {
        OrderResponse order = buildSampleOrderResponse();

        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerName").value("John Doe"));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getOrderById_notFound() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/999"))
            .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById(999L);
    }

    @Test
    void placeOrder_success() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setCustomerName("John Doe");
        request.setPaymentMode("CREDIT_CARD");
        request.setDeliveryAddress("123 Street");
        request.setItems(List.of(new com.example.orderservice.dto.OrderItemRequest("sku1", 2)));

        OrderResponse response = buildSampleOrderResponse();

        when(orderService.placeOrder(any(PlaceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerName").value("John Doe"));

        verify(orderService, times(1)).placeOrder(any(PlaceOrderRequest.class));
    }

    private OrderResponse buildSampleOrderResponse() {
        OrderItemResponse item = OrderItemResponse.builder()
                .skuCode("sku1")
                .quantity(2)
                .pricePerItem(10.0)
                .build();

        return OrderResponse.builder()
                .orderNumber("order123")
                .customerName("John Doe")
                .paymentMode("CREDIT_CARD")
                .deliveryAddress("123 Street")
                .orderStatus("CREATED")
                .orderDate(LocalDateTime.now())
                .totalPrice(20.0)
                .items(List.of(item))
                .build();
    }
}

