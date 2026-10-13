package com.example.inventory.controller;

import com.example.inventory.dto.ReplenishmentResponse;
import com.example.inventory.service.SmartReplenishmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/replenishment")
public class SmartReplenishmentController {

    private final SmartReplenishmentService smartReplenishmentService;

    public SmartReplenishmentController(
            SmartReplenishmentService smartReplenishmentService) {
        this.smartReplenishmentService = smartReplenishmentService;
    }

    @GetMapping("/{productId}/{warehouseId}")
    public ReplenishmentResponse recommendReplenishment(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "7") int leadTimeDays,
            @RequestParam(defaultValue = "10") int safetyStock) {

        return smartReplenishmentService.recommendReplenishment(
                productId,
                warehouseId,
                leadTimeDays,
                safetyStock
        );
    }
}