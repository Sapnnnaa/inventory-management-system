package com.example.inventory.dto;

import java.time.LocalDate;

public record ReplenishmentResponse(
        Long productId,
        Long warehouseId,
        int currentStock,
        int reservedStock,
        int availableStock,
        int leadTimeDays,
        int safetyStock,
        double averageDailyDemand,
        int leadTimeDemand,
        int reorderPoint,
        int suggestedReorderQuantity,
        LocalDate recommendedOrderDate
) {
}