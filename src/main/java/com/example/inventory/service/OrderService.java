package com.example.inventory.service;

import com.example.inventory.dto.OrderItemRequest;
import com.example.inventory.dto.OrderItemResponse;
import com.example.inventory.dto.OrderRequest;
import com.example.inventory.dto.OrderResponse;
import com.example.inventory.dto.OrderStatusRequest;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Order;
import com.example.inventory.entity.OrderItem;
import com.example.inventory.entity.OrderStatus;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Warehouse;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.InvalidOrderStatusException;
import com.example.inventory.exception.InventoryNotFoundException;
import com.example.inventory.exception.OrderNotFoundException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.exception.WarehouseNotFoundException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            InventoryRepository inventoryRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // =========================================================
    // CREATE ORDER
    // =========================================================

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Find warehouse
        Warehouse warehouse = warehouseRepository
                .findById(request.getWarehouseId())
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse not found with id: "
                                        + request.getWarehouseId()
                        )
                );

        // 2. Create new order
        Order order = new Order();

        order.setCustomerName(request.getCustomerName());
        order.setWarehouse(warehouse);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. Process every order item
        for (OrderItemRequest itemRequest : request.getItems()) {

            // Find product
            Product product = productRepository
                    .findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Product not found with id: "
                                            + itemRequest.getProductId()
                            )
                    );

            // Find inventory for this product in this warehouse
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(
                            product.getId(),
                            warehouse.getId()
                    )
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for product id: "
                                            + product.getId()
                                            + " and warehouse id: "
                                            + warehouse.getId()
                            )
                    );


            // Deduct stock
            int availableQuantity =
                    inventory.getQuantity()
                            - inventory.getReservedQuantity();

            if (availableQuantity < itemRequest.getQuantity()){

                throw new InsufficientStockException(
                        "Insufficient available stock for product id: "
                                + product.getId()
                                + ", Available stock: "
                                + availableQuantity
                                + ", requested: "
                                + itemRequest.getQuantity()
                );
            }

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity()
                    + itemRequest.getQuantity()
            );

            inventoryRepository.save(inventory);

            // Create order item
            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());

            // Store product price at the time of order
            orderItem.setPrice(product.getPrice());

            order.getItems().add(orderItem);

            // Calculate item total
            BigDecimal itemTotal = product.getPrice()
                    .multiply(
                            BigDecimal.valueOf(
                                    itemRequest.getQuantity()
                            )
                    );

            totalAmount = totalAmount.add(itemTotal);
        }

        // 4. Set final order total
        order.setTotalAmount(totalAmount);

        // 5. Save order
        Order savedOrder = orderRepository.save(order);

        // 6. Convert entity to response DTO
        return convertToOrderResponse(savedOrder);
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatusRequest request) {

        // 1. Find order
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        // 2. Get current and requested status
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // 3. Check whether transition is allowed
        if (!isValidStatusTransition(
                currentStatus,
                newStatus)) {

            throw new InvalidOrderStatusException(
                    "Invalid order status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        // 4. If order is being confirmed,
       //    convert reserved stock into actual stock deduction
        if (currentStatus == OrderStatus.PENDING
                && newStatus == OrderStatus.CONFIRMED) {

            confirmReservedStock(order);
        }

       // 5. Change status
        order.setStatus(newStatus);

        // 6. Save order
        Order savedOrder = orderRepository.save(order);

        // 7. Convert to response DTO
        return convertToOrderResponse(savedOrder);
    }

    private void confirmReservedStock(Order order){

        Warehouse warehouse = order.getWarehouse();

        for(OrderItem orderItem: order.getItems()) {

            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(
                            orderItem.getProduct().getId(),
                            warehouse.getId()
                    )
                    .orElseThrow(()->
                            new InventoryNotFoundException(
                                    "Inventory not found for product id: "
                                    + orderItem.getProduct().getId()
                                    + " and warehouse id: "
                                    + warehouse.getId()
                            )
                    );

            int reservedQuantity = orderItem.getQuantity();

            //removed reserved quantity
            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() - reservedQuantity
            );

            //remove physically from warehouse stock
            inventory.setQuantity(
                    inventory.getQuantity()- reservedQuantity
            );

            inventoryRepository.save(inventory);
        }
    }

    // =========================================================
    // CHECK STATUS TRANSITION
    // =========================================================

    private boolean isValidStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        if (currentStatus == OrderStatus.PENDING) {

            return newStatus == OrderStatus.CONFIRMED
                    || newStatus == OrderStatus.CANCELLED;
        }

        if (currentStatus == OrderStatus.CONFIRMED) {

            return newStatus == OrderStatus.COMPLETED
                    || newStatus == OrderStatus.CANCELLED;
        }

        // COMPLETED cannot move to another status
        if (currentStatus == OrderStatus.COMPLETED) {

            return false;
        }

        // CANCELLED cannot move to another status
        if (currentStatus == OrderStatus.CANCELLED) {

            return false;
        }

        return false;
    }

    // =========================================================
    // CANCEL ORDER AND RESTORE STOCK
    // =========================================================

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        // 1. Find order
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        // 2. Check if already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {

            throw new InvalidOrderStatusException(
                    "Order is already cancelled"
            );
        }

        // 3. Completed orders cannot be cancelled
        if (order.getStatus() == OrderStatus.COMPLETED) {

            throw new InvalidOrderStatusException(
                    "Completed order cannot be cancelled"
            );
        }


        // 5. Handle inventory based on current order status
        if (order.getStatus() == OrderStatus.PENDING) {

            releaseReservedStock(order);

        } else if (order.getStatus() == OrderStatus.CONFIRMED) {

            restoreConfirmedStock(order);
        }

        // 6. Change order status
        order.setStatus(OrderStatus.CANCELLED);

        // 7. Save order
        Order savedOrder = orderRepository.save(order);

        // 8. Convert to response DTO
        return convertToOrderResponse(savedOrder);
    }

    private void releaseReservedStock(Order order) {

        Warehouse warehouse = order.getWarehouse();

        for (OrderItem orderItem : order.getItems()) {

            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(
                            orderItem.getProduct().getId(),
                            warehouse.getId()
                    )
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for product id: "
                                            + orderItem.getProduct().getId()
                                            + " and warehouse id: "
                                            + warehouse.getId()
                            )
                    );

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity()
                            - orderItem.getQuantity()
            );

            inventoryRepository.save(inventory);
        }
    }

    private void restoreConfirmedStock(Order order) {

        Warehouse warehouse = order.getWarehouse();

        for (OrderItem orderItem : order.getItems()) {

            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(
                            orderItem.getProduct().getId(),
                            warehouse.getId()
                    )
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for product id: "
                                            + orderItem.getProduct().getId()
                                            + " and warehouse id: "
                                            + warehouse.getId()
                            )
                    );

            inventory.setQuantity(
                    inventory.getQuantity()
                            + orderItem.getQuantity()
            );

            inventoryRepository.save(inventory);
        }
    }

    // =========================================================
    // CONVERT ORDER ENTITY TO RESPONSE DTO
    // =========================================================

    private OrderResponse convertToOrderResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());

        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(item -> {

                            OrderItemResponse itemResponse =
                                    new OrderItemResponse();

                            itemResponse.setProductId(
                                    item.getProduct().getId()
                            );

                            itemResponse.setProductName(
                                    item.getProduct().getName()
                            );

                            itemResponse.setQuantity(
                                    item.getQuantity()
                            );

                            itemResponse.setPrice(
                                    item.getPrice()
                            );

                            BigDecimal itemTotal =
                                    item.getPrice()
                                            .multiply(
                                                    BigDecimal.valueOf(
                                                            item.getQuantity()
                                                    )
                                            );

                            itemResponse.setItemTotal(itemTotal);

                            return itemResponse;
                        })
                        .toList();

        response.setItems(itemResponses);

        return response;
    }
}