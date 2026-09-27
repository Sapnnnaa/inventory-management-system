package com.example.inventory.controller;

import com.example.inventory.dto.WarehouseRequest;
import com.example.inventory.entity.Warehouse;
import com.example.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;


@Tag(
        name = "Warehouse Management",
        description = "APIs for managing warehouses"
)
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService){
        this.warehouseService = warehouseService;
    }

    @Operation(
            summary = "Create a new warehouse",
            description = "Creates a warehouse with name, code and location"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Warehouse created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid warehouse data"
            )
    })
    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(
            @Valid @RequestBody WarehouseRequest request) {

        return ResponseEntity.ok(
                warehouseService.createWarehouse(request)
        );
    }

    @Operation(
            summary = "Get all warehouses",
            description = "Returns a list of all warehouses"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Warehouses retrieved successfully"
    )
    @GetMapping
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {

        return ResponseEntity.ok(
                warehouseService.getAllWarehouses()
        );
    }
}
