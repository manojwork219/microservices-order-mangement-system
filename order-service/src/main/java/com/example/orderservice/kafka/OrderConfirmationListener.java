package com.example.orderservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConfirmationListener {

	@KafkaListener(topics = "${order.confirmation.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
	public void listen(String message) {
		System.out.println("✅ Received order confirmation in order-service: " + message);
		// You can later update order status in DB here
	}
}
