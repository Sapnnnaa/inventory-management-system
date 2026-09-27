package com.example.inventory.controller;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.entity.Inventory;
import com.example.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Inventory Management",
        description = "APIs for managing inventory and stock"
)
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(
            summary = "Get all inventory",
            description = "Returns all inventory records across warehouses"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Inventory retrieved successfully"
    )
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }


    @Operation(
            summary = "Get inventory by ID",
            description = "Returns a specific inventory record using its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory found successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(id)
        );
    }


    @Operation(
            summary = "Create inventory",
            description = "Creates an inventory record for a product in a warehouse"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid inventory data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product or warehouse not found"
            )
    })
    @PostMapping
    public ResponseEntity<Inventory> createInventory(
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.createInventory(request)
        );
    }


    @Operation(
            summary = "Stock in",
            description = "Adds stock quantity to an existing inventory record"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock added successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid stock quantity"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found"
            )
    })
    @PostMapping("/stock-in")
    public ResponseEntity<Inventory> stockIn(
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.stockIn(request)
        );
    }


    @Operation(
            summary = "Stock out",
            description = "Removes available stock from an inventory record"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock removed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid quantity or insufficient available stock"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found"
            )
    })
    @PostMapping("/stock-out")
    public ResponseEntity<Inventory> stockOut(
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.stockOut(request)
        );
    }
}