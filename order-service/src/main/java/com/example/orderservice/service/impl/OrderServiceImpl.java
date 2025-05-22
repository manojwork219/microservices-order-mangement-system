package com.example.orderservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.common.dto.OrderItemEvent;
import com.example.common.dto.OrderPlacedEvent;
import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderItemResponse;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.PlaceOrderRequest;
import com.example.orderservice.exception.InventoryInsufficientException;
import com.example.orderservice.exception.InventoryNotFoundException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.kafka.OrderKafkaProducer;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final RestTemplate restTemplate;
	private final OrderKafkaProducer orderKafkaProducer;

	@Value("${inventory.service.url}")
	private String inventoryServiceUrl;

	public OrderServiceImpl(OrderRepository orderRepository, RestTemplate restTemplate,
			OrderKafkaProducer orderKafkaProducer) {
		this.orderRepository = orderRepository;
		this.restTemplate = restTemplate;
		this.orderKafkaProducer = orderKafkaProducer;
	}

	public void setInventoryServiceUrl(String inventoryServiceUrl) {
		this.inventoryServiceUrl = inventoryServiceUrl;
	}

	@Override
	public OrderResponse placeOrder(PlaceOrderRequest orderRequest) {
		List<OrderItem> orderItems = new ArrayList<>();
		double totalPrice = 0.0;

		for (OrderItemRequest itemRequest : orderRequest.getItems()) {
			String url = inventoryServiceUrl + "/" + itemRequest.getSkuCode();

			InventoryResponse inventory;
			try {
				ResponseEntity<InventoryResponse> response = restTemplate.getForEntity(url, InventoryResponse.class);
				if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
					inventory = response.getBody();

					if (inventory.getQuantity() < itemRequest.getQuantity()) {
						throw new InventoryInsufficientException(
								"Insufficient inventory for skuCode: " + itemRequest.getSkuCode());
					}
				} else {
					throw new InventoryNotFoundException(
							"Inventory not found for skuCode: " + itemRequest.getSkuCode());
				}
			}

			// change
			catch (HttpClientErrorException e) {
				if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
					throw new InventoryNotFoundException(
							"Inventory not found for skuCode: " + itemRequest.getSkuCode());
				}
				throw new InventoryNotFoundException("Error from inventory service: " + e.getStatusCode());
			}

			catch (RestClientException ex) {
				throw new InventoryNotFoundException("Unable to reach inventory service");
			}

			OrderItem orderItem = OrderItem.builder().skuCode(itemRequest.getSkuCode())
					.quantity(itemRequest.getQuantity()).pricePerItem(inventory.getPricePerItem()).build();

			totalPrice += inventory.getPricePerItem() * itemRequest.getQuantity();
			orderItems.add(orderItem);
		}

		Order order = Order.builder().orderNumber(UUID.randomUUID().toString())
				.customerName(orderRequest.getCustomerName()).paymentMode(orderRequest.getPaymentMode())
				.deliveryAddress(orderRequest.getDeliveryAddress()).orderStatus("CREATED")
				.orderDate(LocalDateTime.now()).totalPrice(totalPrice).items(orderItems).build();

		orderItems.forEach(item -> item.setOrder(order)); // Set order reference in each item

		orderRepository.save(order);

		// Simplified Kafka message
		List<OrderItemEvent> itemEvents = order.getItems().stream()
				.map(item -> new OrderItemEvent(item.getSkuCode(), item.getQuantity(), item.getPricePerItem()))
				.toList();

		OrderPlacedEvent event = new OrderPlacedEvent(order.getOrderNumber(), itemEvents);
		orderKafkaProducer.sendMessage(event);

		return mapToOrderResponse(order);
	}

	@Override
	public OrderResponse getOrderById(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found."));

		return mapToOrderResponse(order);
	}

	@Override
	public List<OrderResponse> getAllOrders() {
		List<Order> orders = orderRepository.findAll();
		return orders.stream().map(this::mapToOrderResponse).toList();
	}

	@Override
	public OrderResponse mapToOrderResponse(Order order) {
		List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> OrderItemResponse.builder()
				.skuCode(item.getSkuCode()).quantity(item.getQuantity()).pricePerItem(item.getPricePerItem()).build())
				.toList();

		return OrderResponse.builder().orderNumber(order.getOrderNumber()).customerName(order.getCustomerName())
				.paymentMode(order.getPaymentMode()).deliveryAddress(order.getDeliveryAddress())
				.orderStatus(order.getOrderStatus()).orderDate(order.getOrderDate()).totalPrice(order.getTotalPrice())
				.items(itemResponses).build();
	}

}
