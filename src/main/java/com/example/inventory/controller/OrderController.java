package com.example.inventory.controller;

import com.example.inventory.dto.OrderRequest;
import com.example.inventory.dto.OrderResponse;
import com.example.inventory.dto.OrderStatusRequest;
import com.example.inventory.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(
        name = "Order Management",
        description = "APIs for creating and managing customer orders"
)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {

        this.orderService = orderService;
    }

    @Operation(
            summary = "Create a new order",
            description = "Creates a new order and reserves available stock for the ordered products"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order created successfully and stock reserved"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order data or insufficient stock"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product, warehouse or inventory not found"
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request) {

        return ResponseEntity.ok(
                orderService.createOrder(request)
        );
    }

    @Operation(
            summary = "Update order status",
            description = "Updates the status of an order and performs the required stock operation"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order status updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order status transition"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found"
            )
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(id, request)
        );
    }

    @Operation(
            summary = "Cancel an order",
            description = "Cancels an order and restores or releases stock according to the current order status"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order cannot be cancelled"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found"
            )
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.cancelOrder(id)
        );
    }
}