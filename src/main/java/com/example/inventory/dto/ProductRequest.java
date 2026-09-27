package com.example.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequest {

    @Schema(
            description = "Name of the product",
            example = "iPhone 15",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Product name is required")
    private String name;


    @Schema(
            description = "Unique SKU of the product",
            example = "IPH15-128GB",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "SKU is required")
    private String sku;


    @Schema(
            description = "Description of the product",
            example = "Apple iPhone 15 with 128GB storage"
    )
    private String description;


    @Schema(
            description = "Price of the product",
            example = "69999.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;
}