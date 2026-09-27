package com.example.inventory.dto;

import com.example.inventory.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusRequest {

    @Schema(
            description = "New status of the order",
            example = "CONFIRMED",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Order status is required")
    private OrderStatus status;
}