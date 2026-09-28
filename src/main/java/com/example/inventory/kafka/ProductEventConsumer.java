package com.example.inventory.kafka;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer {

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000)
    )

    @KafkaListener(
            topics = "product-created",
            groupId = "inventory-group"
    )
    public void consumeProductCrestedEvent(ProductEvent event){

        System.out.println("******** CONSUMER 1 ********");
        System.out.println("Product event received from Kafka");
        System.out.println("Product Id: " + event.getProductId());
        System.out.println("Product Name: "+ event.getName());
        System.out.println("Product SKU: "+ event.getSku());
        System.out.println("Product Price: "+ event.getPrice());
        System.out.println("========================");

        // Deliberately create an error for testing
        if (event.getName().equalsIgnoreCase("FAIL")) {
            throw new RuntimeException("Testing kafka failure");
        }

        System.out.println("===============");
    }

    @DltHandler
    public void handleDlt(ProductEvent event) {

        System.out.println("********DLT*******");
        System.out.println("Message moved to Dead Letter Topic");
        System.out.println("Product Id: " + event.getProductId());
        System.out.println("Product Name: " + event.getName());
        System.out.println("==============");
    }
}
