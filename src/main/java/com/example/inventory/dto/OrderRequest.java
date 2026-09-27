package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {

    @Schema(
            description = "Name of the customer placing the order",
            example = "Sapna",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Customer name is required")
    private String customerName;


    @Schema(
            description = "ID of the warehouse from which stock will be reserved",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;


    @Schema(
            description = "Products included in the order",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
}