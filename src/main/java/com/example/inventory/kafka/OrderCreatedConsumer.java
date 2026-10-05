package com.example.inventory.kafka;

import com.example.inventory.entity.ProcessedEvent;
import com.example.inventory.repository.ProcessedEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderCreatedConsumer {

    private final ProcessedEventRepository processedEventRepository;

    public OrderCreatedConsumer(
            ProcessedEventRepository processedEventRepository) {

        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "order-group"
    )
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {

        String eventId = String.valueOf(event.getOrderId());

        // Check whether event was already processed
        if (processedEventRepository.existsByEventId(eventId)) {

            System.out.println(
                    "Duplicate event ignored: " + eventId
            );

            return;
        }

        System.out.println("******** ORDER CONSUMER ********");

        System.out.println(
                "Order event received from Kafka"
        );

        System.out.println(
                "Order Id: " + event.getOrderId()
        );

        System.out.println(
                "Customer Name: " + event.getCustomerName()
        );

        System.out.println(
                "Warehouse Id: " + event.getWarehouseId()
        );

        System.out.println(
                "Total Amount: " + event.getTotalAmount()
        );

        event.getItems().forEach(item -> {

            System.out.println(
                    "Product Id: "
                            + item.getProductId()
                            + ", Quantity: "
                            + item.getQuantity()
            );
        });

        // Mark event as processed
        ProcessedEvent processedEvent =
                new ProcessedEvent();

        processedEvent.setEventId(eventId);

        processedEventRepository.save(processedEvent);

        System.out.println(
                "Event processed successfully: " + eventId
        );

        System.out.println("==============================");
    }
}