package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryRequest {

    @Schema(
            description = "ID of the product",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Product ID is required")
    private Long productId;


    @Schema(
            description = "ID of the warehouse",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;


    @Schema(
            description = "Quantity of stock",
            example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}