package com.example.inventory.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer {

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
    }
}
