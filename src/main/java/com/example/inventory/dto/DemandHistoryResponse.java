package com.example.inventory.dto;

import java.time.LocalDate;

public record DemandHistoryResponse(
        Long id,
        Long productId,
        Long warehouseId,
        LocalDate demandDate,
        Integer quantity
) {
}