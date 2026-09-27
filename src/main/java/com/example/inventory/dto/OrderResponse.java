package com.example.inventory.dto;

import com.example.inventory.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {

    @Schema(
            description = "Unique ID of the order",
            example = "10"
    )
    private Long id;


    @Schema(
            description = "Name of the customer",
            example = "Sapna"
    )
    private String customerName;


    @Schema(
            description = "Date and time when the order was created",
            example = "2026-09-14T16:30:00"
    )
    private LocalDateTime orderDate;


    @Schema(
            description = "Current status of the order",
            example = "PENDING"
    )
    private OrderStatus status;


    @Schema(
            description = "Total amount of the order",
            example = "139998.00"
    )
    private BigDecimal totalAmount;


    @Schema(
            description = "Items included in the order"
    )
    private List<OrderItemResponse> items;
}