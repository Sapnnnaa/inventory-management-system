
package com.example.inventory.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class InventoryAiService {

    private final ChatClient chatClient;
    private final InventoryAiTools inventoryAiTools;

    public InventoryAiService(
            ChatClient.Builder chatClientBuilder,
            InventoryAiTools inventoryAiTools) {

        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are an AI Inventory Assistant for an
                        Inventory and Order Management System.

                        Rules:
                        1. Use the available tools for questions that
                           require actual product or stock data.
                        2. Never invent product details, stock levels,
                           prices, or forecast values.
                        3. Explain stock and replenishment results in
                           simple, clear language.
                        4. If a required product ID or warehouse ID is
                           missing, ask the user for it.
                        5. For replenishment questions, use a default
                           lead time of 7 days and safety stock of
                           10 units only when the user has not supplied
                           different values.
                        6. You can read information only. Never claim
                           that you created an order or changed stock.
                        7. If a tool reports that a record was not found
                           or returns an error, explain that clearly.
                        """)
                .build();

        this.inventoryAiTools = inventoryAiTools;
    }

    public String ask(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty."
            );
        }

        if (question.length() > 2000) {
            throw new IllegalArgumentException(
                    "Question must not exceed 2000 characters."
            );
        }

        return chatClient.prompt()
                .user(question)
                .tools(inventoryAiTools)
                .call()
                .content();
    }
}
