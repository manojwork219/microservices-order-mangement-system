package com.example.inventoryservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryConfirmationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${order.confirmation.topic.name}")
    private String confirmationTopic;

    public InventoryConfirmationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendConfirmation(String message) {
        kafkaTemplate.send(confirmationTopic, message);
        System.out.println("📤 Sent confirmation to order-service: " + message);
    }
}
