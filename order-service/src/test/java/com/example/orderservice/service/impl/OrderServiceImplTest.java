package com.example.orderservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.common.dto.OrderPlacedEvent;
import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.PlaceOrderRequest;
import com.example.orderservice.exception.InventoryInsufficientException;
import com.example.orderservice.exception.InventoryNotFoundException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.kafka.OrderKafkaProducer;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;

class OrderServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderKafkaProducer orderKafkaProducer;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private String inventoryServiceUrl = "http://inventory-service/api/inventory";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        orderService.setInventoryServiceUrl("http://inventory-service/api/inventory"); // needed if not using @Value
    }

    @Test
    void placeOrder_success() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setCustomerName("John");
        request.setPaymentMode("CASH");
        request.setDeliveryAddress("123 Street");
        request.setItems(List.of(new OrderItemRequest("sku1", 2)));

        InventoryResponse inventoryResponse = new InventoryResponse();
        inventoryResponse.setSkuCode("sku1");
        inventoryResponse.setQuantity(10);
        inventoryResponse.setPricePerItem(5.0);

        when(restTemplate.getForEntity(eq(inventoryServiceUrl + "/sku1"), eq(InventoryResponse.class)))
            .thenReturn(new ResponseEntity<>(inventoryResponse, HttpStatus.OK));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        assertEquals("John", response.getCustomerName());
        assertEquals(10.0, response.getTotalPrice());
        assertEquals(1, response.getItems().size());
        assertEquals("sku1", response.getItems().get(0).getSkuCode());

        verify(restTemplate, times(1)).getForEntity(anyString(), eq(InventoryResponse.class));
        verify(orderRepository).save(orderCaptor.capture());
        verify(orderKafkaProducer).sendMessage(any(OrderPlacedEvent.class));

        assertEquals(10.0, orderCaptor.getValue().getTotalPrice());
    }

    @Test
    void placeOrder_inventoryInsufficient_throwsException() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(new OrderItemRequest("sku1", 5)));

        InventoryResponse inventoryResponse = new InventoryResponse();
        inventoryResponse.setSkuCode("sku1");
        inventoryResponse.setQuantity(3);
        inventoryResponse.setPricePerItem(5.0);

        when(restTemplate.getForEntity(anyString(), eq(InventoryResponse.class)))
            .thenReturn(new ResponseEntity<>(inventoryResponse, HttpStatus.OK));

        InventoryInsufficientException ex = assertThrows(InventoryInsufficientException.class, () -> {
            orderService.placeOrder(request);
        });

        assertTrue(ex.getMessage().contains("Insufficient inventory"));
        verify(orderRepository, never()).save(any());
        verify(orderKafkaProducer, never()).sendMessage(any());
    }

    @Test
    void placeOrder_inventoryNotFound_throwsException() {
        // given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .customerName("Manoj")
                .paymentMode("UPI")
                .deliveryAddress("Address")
                .items(List.of(OrderItemRequest.builder()
                        .skuCode("SKU404")
                        .quantity(1)
                        .build()))
                .build();

        String inventoryUrl = "http://dummy-inventory-service/SKU404";
        when(restTemplate.getForEntity(eq(inventoryUrl), eq(InventoryResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        orderService.setInventoryServiceUrl("http://dummy-inventory-service");

        // when + then
        Exception exception = assertThrows(InventoryNotFoundException.class, () -> {
            orderService.placeOrder(request);
        });

        assertEquals("Inventory not found for skuCode: SKU404", exception.getMessage());
    }


    @Test
    void getOrderById_found_returnsOrderResponse() {
        Order mockOrder = buildSampleOrder();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(mockOrder.getCustomerName(), response.getCustomerName());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(100L);
        });

        verify(orderRepository).findById(100L);
    }

    @Test
    void getAllOrders_returnsList() {
        Order mockOrder = buildSampleOrder();
        when(orderRepository.findAll()).thenReturn(List.of(mockOrder));

        List<OrderResponse> allOrders = orderService.getAllOrders();

        assertNotNull(allOrders);
        assertEquals(1, allOrders.size());
        assertEquals(mockOrder.getCustomerName(), allOrders.get(0).getCustomerName());
        verify(orderRepository).findAll();
    }

    private Order buildSampleOrder() {
        OrderItem item = OrderItem.builder()
                .skuCode("sku1")
                .quantity(2)
                .pricePerItem(10.0)
                .build();

        Order order = Order.builder()
                .orderNumber("order123")
                .customerName("John Doe")
                .paymentMode("CASH")
                .deliveryAddress("123 Street")
                .orderStatus("CREATED")
                .orderDate(LocalDateTime.now())
                .totalPrice(20.0)
                .items(List.of(item))
                .build();

        item.setOrder(order);

        return order;
    }
}
