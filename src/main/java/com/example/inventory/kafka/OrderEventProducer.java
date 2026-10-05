package com.example.inventory.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventProducer {

    private static final String TOPIC = "order-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, Object>>
    sendOrderCreatedEvent(OrderCreatedEvent event) {

        return kafkaTemplate.send(
                TOPIC,
                event.getOrderId().toString(),
                event
        );
    }
}