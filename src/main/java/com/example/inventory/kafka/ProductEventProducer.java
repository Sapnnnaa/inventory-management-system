package com.example.inventory.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public ProductEventProducer(
            KafkaTemplate<String, ProductEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProductCreatedEvent(ProductEvent event) {

        kafkaTemplate.send(
                "product-created",
                event.getProductId().toString(),
                event
        );

        System.out.println("Product event sent to Kafka: "
                + event.getProductId());
    }
}