package com.example.inventoryservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.common.dto.OrderItemEvent;
import com.example.common.dto.OrderPlacedEvent;
import com.example.inventoryservice.service.InventoryService;

@Service
public class OrderKafkaListener {

    private final InventoryService inventoryService;

    public OrderKafkaListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "${order.topic.name}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(OrderPlacedEvent event) {
        System.out.println("✅ Received OrderPlacedEvent: " + event.getOrderNumber());

        for (OrderItemEvent item : event.getOrderItems()) {
            inventoryService.updateInventoryQuantity(item.getSkuCode(), -item.getQuantity(), item.getPricePerItem());
        }
    }

}
