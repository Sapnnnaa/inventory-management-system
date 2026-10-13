package com.example.inventory.service;

import com.example.inventory.dto.DemandForecastResponse;
import com.example.inventory.dto.ReplenishmentResponse;
import com.example.inventory.entity.Inventory;
import com.example.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SmartReplenishmentService {

    private final InventoryRepository inventoryRepository;
    private final DemandForecastService demandForecastService;

    public SmartReplenishmentService(
            InventoryRepository inventoryRepository,
            DemandForecastService demandForecastService) {

        this.inventoryRepository = inventoryRepository;
        this.demandForecastService = demandForecastService;
    }

    @Transactional(readOnly = true)
    public ReplenishmentResponse recommendReplenishment(
            Long productId,
            Long warehouseId,
            int leadTimeDays,
            int safetyStock) {

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be positive"
            );
        }

        if (warehouseId == null || warehouseId <= 0) {
            throw new IllegalArgumentException(
                    "Warehouse ID must be positive"
            );
        }

        if (leadTimeDays < 1 || leadTimeDays > 90) {
            throw new IllegalArgumentException(
                    "Lead time must be between 1 and 90 days"
            );
        }

        if (safetyStock < 0) {
            throw new IllegalArgumentException(
                    "Safety stock cannot be negative"
            );
        }

        Inventory inventory =
                inventoryRepository
                        .findInventoryForReplenishment(
                                productId,
                                warehouseId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Inventory not found for this product and warehouse"
                                )
                        );

        int currentStock = inventory.getQuantity();

        int reservedStock = inventory.getReservedQuantity() == null
                ? 0
                : inventory.getReservedQuantity();

        if (currentStock < 0
                || reservedStock < 0
                || reservedStock > currentStock) {

            throw new IllegalStateException(
                    "Inventory contains invalid stock quantities"
            );
        }

        int availableStock = currentStock - reservedStock;

        DemandForecastResponse forecast =
                demandForecastService.forecastDemand(
                        productId,
                        warehouseId,
                        leadTimeDays
                );

        double averageDailyDemand =
                forecast.averageDailyDemand();

        int leadTimeDemand =
                forecast.totalForecastDemand();

        int reorderPoint =
                leadTimeDemand + safetyStock;

        int suggestedReorderQuantity =
                Math.max(0, reorderPoint - availableStock);

        LocalDate recommendedOrderDate =
                suggestedReorderQuantity > 0
                        ? LocalDate.now()
                        : null;

        return new ReplenishmentResponse(
                productId,
                warehouseId,
                currentStock,
                reservedStock,
                availableStock,
                leadTimeDays,
                safetyStock,
                averageDailyDemand,
                leadTimeDemand,
                reorderPoint,
                suggestedReorderQuantity,
                recommendedOrderDate
        );
    }
}