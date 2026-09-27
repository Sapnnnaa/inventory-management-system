package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemResponse {

    @Schema(
            description = "ID of the product",
            example = "1"
    )
    private Long productId;


    @Schema(
            description = "Name of the product",
            example = "iPhone 15"
    )
    private String productName;


    @Schema(
            description = "Quantity ordered",
            example = "2"
    )
    private Integer quantity;


    @Schema(
            description = "Price of the product at the time of order",
            example = "69999.00"
    )
    private BigDecimal price;


    @Schema(
            description = "Total amount for this order item",
            example = "139998.00"
    )
    private BigDecimal itemTotal;
}