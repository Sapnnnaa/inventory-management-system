package com.example.inventory.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer2 {

    @KafkaListener(
            topics = "product-created",
            groupId = "inventory-group-2"
    )
    public void consumeProductCreatedEvent(ProductEvent event) {

        System.out.println("******** CONSUMER 2 ********");
        System.out.println("Product event received from Kafka");
        System.out.println("Product ID: " + event.getProductId());
        System.out.println("Product Name: " + event.getName());
        System.out.println("Product SKU: "+ event.getSku());
        System.out.println("Product Price: "+ event.getPrice());
        System.out.println("*****************************");
    }
}