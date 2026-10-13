package com.example.inventory.ai;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final InventoryAiService inventoryAiService;

    public AiChatController(InventoryAiService inventoryAiService) {
        this.inventoryAiService = inventoryAiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @RequestBody AiChatRequest request) {

        if (request == null
                || request.question() == null
                || request.question().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new AiChatResponse(
                            "Please provide a question."
                    ));
        }

        if (request.question().length() > 2000) {
            return ResponseEntity.badRequest()
                    .body(new AiChatResponse(
                            "Question must not exceed 2000 characters."
                    ));
        }

        String answer = inventoryAiService.ask(request.question());

        return ResponseEntity.ok(new AiChatResponse(answer));
    }
}