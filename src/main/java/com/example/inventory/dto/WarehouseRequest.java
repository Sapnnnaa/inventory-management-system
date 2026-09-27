package com.example.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
public class WarehouseRequest {

    @Schema(
            description = "Name of the warehouse",
            example = "Main Warehouse",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Warehouse name is required")
    private String name;


    @Schema(
            description = "Unique code of the warehouse",
            example = "WH-001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Warehouse code is required")
    private String code;


    @Schema(
            description = "Location of the warehouse",
            example = "Dhanbad",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Warehouse location is required")
    private String location;
}