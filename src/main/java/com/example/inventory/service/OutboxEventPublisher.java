package com.example.inventory.service;

import com.example.inventory.entity.OutboxEvent;
import com.example.inventory.kafka.OrderCreatedEvent;
import com.example.inventory.kafka.OrderEventProducer;
import com.example.inventory.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            OrderEventProducer orderEventProducer,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.orderEventProducer = orderEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findByPublishedFalse();

        for (OutboxEvent outboxEvent : events) {

            try {

                if (outboxEvent.getEventType()
                        .equals("ORDER_CREATED")) {

                    OrderCreatedEvent event =
                            objectMapper.readValue(
                                    outboxEvent.getPayload(),
                                    OrderCreatedEvent.class
                            );

                    // Send to Kafka and wait for Kafka acknowledgement
                    orderEventProducer
                            .sendOrderCreatedEvent(event)
                            .get(10, TimeUnit.SECONDS);

                    // Kafka successfully acknowledged the message
                    outboxEvent.setPublished(true);

                    outboxEventRepository.save(outboxEvent);

                    System.out.println(
                            "Outbox event published successfully: "
                                    + outboxEvent.getEventId()
                    );
                }

            } catch (Exception e) {

                System.out.println(
                        "Failed to publish outbox event: "
                                + outboxEvent.getEventId()
                );

                e.printStackTrace();
            }
        }
    }
}