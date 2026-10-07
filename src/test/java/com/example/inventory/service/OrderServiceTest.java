package com.example.inventory.service;

import com.example.inventory.dto.OrderItemRequest;
import com.example.inventory.dto.OrderRequest;
import com.example.inventory.dto.OrderResponse;
import com.example.inventory.dto.OrderStatusRequest;
import com.example.inventory.entity.*;
import com.example.inventory.exception.*;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.OutboxEventRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.WarehouseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws Exception {

        // Arrange

        // Create order request
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Sapna");
        request.setWarehouseId(1L);
        request.setItems(List.of(itemRequest));


        // Create warehouse
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);


        // Create product
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));


        // Create inventory
        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(10);
        inventory.setReservedQuantity(2);


        // Mock warehouse
        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(warehouse));


        // Mock product
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));


        // Mock inventory
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));


        // Mock inventory save
        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);


        // Mock order save
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {

                    Order order = invocation.getArgument(0);

                    order.setId(100L);

                    return order;
                });


        // Mock ObjectMapper
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"orderId\":100}");


        // Mock Outbox save
        when(outboxEventRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        OrderResponse result = orderService.createOrder(request);


        // Assert
        assertNotNull(result);

        assertEquals(100L, result.getId());

        assertEquals("Sapna", result.getCustomerName());

        assertEquals(OrderStatus.PENDING, result.getStatus());

        assertEquals(
                new BigDecimal("120000.00"),
                result.getTotalAmount()
        );

        assertEquals(1, result.getItems().size());

        assertEquals(1L, result.getItems().get(0).getProductId());

        assertEquals(2, result.getItems().get(0).getQuantity());

        assertEquals(
                new BigDecimal("60000.00"),
                result.getItems().get(0).getPrice()
        );


        // Verify stock reservation
        assertEquals(10, inventory.getQuantity());

        assertEquals(4, inventory.getReservedQuantity());


        // Verify repository calls

        verify(warehouseRepository)
                .findById(1L);

        verify(productRepository)
                .findById(1L);

        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository)
                .save(inventory);

        verify(orderRepository)
                .save(any(Order.class));

        verify(objectMapper)
                .writeValueAsString(any());

        verify(outboxEventRepository)
                .save(any());
    }

    @Test
    void createOrder_shouldThrowExceptionWhenStockIsInsufficient() {

        // Arrange

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(10);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Sapna");
        request.setWarehouseId(1L);
        request.setItems(List.of(itemRequest));

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(10);
        inventory.setReservedQuantity(3);

        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(warehouse));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        // Act & Assert

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(request)
        );

        // Verify

        assertEquals(10, inventory.getQuantity());
        assertEquals(3, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(outboxEventRepository, never())
                .save(any());
    }

    @Test
    void createOrder_shouldThrowExceptionWhenWarehouseNotFound() {

        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Sapna");
        request.setWarehouseId(999L);
        request.setItems(List.of(itemRequest));

        when(warehouseRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                WarehouseNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        // Verify
        verify(warehouseRepository)
                .findById(999L);

        verify(productRepository, never())
                .findById(anyLong());

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(outboxEventRepository, never())
                .save(any());
    }

    @Test
    void createOrder_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(999L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Sapna");
        request.setWarehouseId(1L);
        request.setItems(List.of(itemRequest));

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        // Warehouse exists
        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(warehouse));

        // Product does not exist
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        // Verify
        verify(warehouseRepository)
                .findById(1L);

        verify(productRepository)
                .findById(999L);

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(outboxEventRepository, never())
                .save(any());
    }

    @Test
    void createOrder_shouldThrowExceptionWhenInventoryNotFound() {

        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerName("Sapna");
        request.setWarehouseId(1L);
        request.setItems(List.of(itemRequest));

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));

        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(warehouse));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        // Inventory does not exist
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        // Verify
        verify(warehouseRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(outboxEventRepository, never())
                .save(any());
    }

    @Test
    void updateOrderStatus_shouldConfirmPendingOrderAndReduceStock() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setCustomerName("Sapna");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        order.setWarehouse(warehouse);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("120000.00"));

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));

        com.example.inventory.entity.OrderItem orderItem =
                new com.example.inventory.entity.OrderItem();

        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(new BigDecimal("60000.00"));
        orderItem.setOrder(order);

        order.getItems().add(orderItem);

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setQuantity(10);
        inventory.setReservedQuantity(2);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);

        OrderStatusRequest statusRequest = new OrderStatusRequest();
        statusRequest.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        // Act
        OrderResponse response =
                orderService.updateOrderStatus(100L, statusRequest);

        // Assert
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());

        assertEquals(8, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());

        // Verify
        verify(orderRepository).findById(100L);

        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository)
                .save(inventory);

        verify(orderRepository)
                .save(order);
    }

    @Test
    void updateOrderStatus_shouldThrowExceptionForInvalidStatusTransition() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.PENDING);

        OrderStatusRequest statusRequest = new OrderStatusRequest();
        statusRequest.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.updateOrderStatus(100L, statusRequest)
        );

        // Verify
        verify(orderRepository).findById(100L);

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldReleaseReservedStockForPendingOrder() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setCustomerName("Sapna");
        order.setStatus(OrderStatus.PENDING);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        order.setWarehouse(warehouse);

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(new BigDecimal("60000.00"));

        order.getItems().add(orderItem);

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setQuantity(10);
        inventory.setReservedQuantity(2);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        // Act
        OrderResponse response =
                orderService.cancelOrder(100L);

        // Assert
        assertEquals(OrderStatus.CANCELLED, response.getStatus());

        assertEquals(10, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());

        // Verify
        verify(orderRepository).findById(100L);

        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository)
                .save(inventory);

        verify(orderRepository)
                .save(order);
    }

    @Test
    void cancelOrder_shouldRestoreStockForConfirmedOrder() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setCustomerName("Sapna");
        order.setStatus(OrderStatus.CONFIRMED);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        order.setWarehouse(warehouse);

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(new BigDecimal("60000.00"));

        order.getItems().add(orderItem);

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setQuantity(8);
        inventory.setReservedQuantity(0);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        // Act
        OrderResponse response =
                orderService.cancelOrder(100L);

        // Assert
        assertEquals(OrderStatus.CANCELLED, response.getStatus());

        assertEquals(10, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());

        // Verify
        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository)
                .save(inventory);

        verify(orderRepository)
                .save(order);
    }

    @Test
    void updateOrderStatus_shouldThrowExceptionWhenOrderNotFound() {

        // Arrange
        OrderStatusRequest statusRequest = new OrderStatusRequest();
        statusRequest.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrderStatus(999L, statusRequest)
        );

        // Verify
        verify(orderRepository).findById(999L);

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldThrowExceptionWhenOrderNotFound() {

        // Arrange
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(999L)
        );

        // Verify
        verify(orderRepository).findById(999L);

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldThrowExceptionWhenOrderAlreadyCancelled() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.cancelOrder(100L)
        );

        // Verify
        verify(orderRepository).findById(100L);

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldThrowExceptionWhenOrderIsCompleted() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.cancelOrder(100L)
        );

        // Verify
        verify(orderRepository).findById(100L);

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldCancelPendingOrderWithoutChangingStock() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setCustomerName("Sapna");
        order.setStatus(OrderStatus.PENDING);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        order.setWarehouse(warehouse);

        OrderStatusRequest statusRequest =
                new OrderStatusRequest();

        statusRequest.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        // Act
        OrderResponse response =
                orderService.updateOrderStatus(100L, statusRequest);

        // Assert
        assertEquals(OrderStatus.CANCELLED, response.getStatus());

        // No inventory operation should happen
        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(orderRepository)
                .save(order);
    }

    @Test
    void updateOrderStatus_shouldCompleteConfirmedOrder() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setCustomerName("Sapna");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(new BigDecimal("120000.00"));

        OrderStatusRequest statusRequest =
                new OrderStatusRequest();

        statusRequest.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        // Act
        OrderResponse response =
                orderService.updateOrderStatus(100L, statusRequest);

        // Assert
        assertEquals(
                OrderStatus.COMPLETED,
                response.getStatus()
        );

        // Verify
        verify(orderRepository).findById(100L);

        verify(orderRepository).save(order);

        // No inventory operation should happen
        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void updateOrderStatus_shouldThrowExceptionWhenCompletedOrderIsUpdated() {

        // Arrange
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.COMPLETED);

        OrderStatusRequest statusRequest =
                new OrderStatusRequest();

        statusRequest.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.updateOrderStatus(100L, statusRequest)
        );

        // Verify
        verify(orderRepository).findById(100L);

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(inventoryRepository, never())
                .findByProductIdAndWarehouseId(anyLong(), anyLong());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }
}