package com.example.inventory.service;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Warehouse;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.InventoryNotFoundException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void createInventory_shouldCreateInventorySuccessfully() {

        // Arrange
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(50);

        Product product = new Product();
        product.setId(1L);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        Inventory savedInventory = new Inventory();
        savedInventory.setId(10L);
        savedInventory.setProduct(product);
        savedInventory.setWarehouse(warehouse);
        savedInventory.setQuantity(50);
        savedInventory.setReservedQuantity(0);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(warehouse));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(savedInventory);

        // Act
        Inventory result = inventoryService.createInventory(request);

        //Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(50, result.getQuantity());
        assertEquals(0, result.getReservedQuantity());
        assertEquals(product, result.getProduct());
        assertEquals(warehouse, result.getWarehouse());

        verify(productRepository).findById(1L);
        verify(warehouseRepository).findById(1L);
        verify(inventoryRepository).save(any(Inventory.class));

    }

    @Test
    void getInventoryById_shouldReturnInventorySuccessfully() {

        // Arrange
        Long inventoryId = 10L;

        Product product = new Product();
        product.setId(1L);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(50);
        inventory.setReservedQuantity(0);

        when(inventoryRepository.findById(inventoryId))
                .thenReturn(Optional.of(inventory));

        // Act
        Inventory result = inventoryService.getInventoryById(inventoryId);

        // Assert
        assertNotNull(result);
        assertEquals(inventoryId, result.getId());
        assertEquals(50, result.getQuantity());
        assertEquals(0, result.getReservedQuantity());
        assertEquals(product, result.getProduct());
        assertEquals(warehouse, result.getWarehouse());

        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void getInventoryById_shouldThrowExceptionWhenInventoryNotFound() {

        // Arrange
        Long inventoryId = 999L;

        when(inventoryRepository.findById(inventoryId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.getInventoryById(inventoryId)
        );

        // Verify
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void stockIn_shouldIncreaseQuantityWhenInventoryExists() {

        // Arrange
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(20);

        Product product = new Product();
        product.setId(1L);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(50);
        inventory.setReservedQuantity(10);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        // Act
        Inventory result = inventoryService.stockIn(request);

        // Assert
        assertNotNull(result);
        assertEquals(70, result.getQuantity());
        assertEquals(10, result.getReservedQuantity());

        // Verify
        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void stockIn_shouldCreateNewInventoryWhenInventoryDoesNotExist() {

        // Arrange
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(30);

        Product product = new Product();
        product.setId(1L);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        Inventory savedInventory = new Inventory();
        savedInventory.setId(20L);
        savedInventory.setProduct(product);
        savedInventory.setWarehouse(warehouse);
        savedInventory.setQuantity(30);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(warehouse));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(savedInventory);

        // Act
        Inventory result = inventoryService.stockIn(request);

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals(30, result.getQuantity());
        assertEquals(product, result.getProduct());
        assertEquals(warehouse, result.getWarehouse());

        // Verify
        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(productRepository).findById(1L);

        verify(warehouseRepository).findById(1L);

        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void stockOut_shouldDecreaseQuantitySuccessfully() {

        // Arrange
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(20);

        Product product = new Product();
        product.setId(1L);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(50);
        inventory.setReservedQuantity(10);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        // Act
        Inventory result = inventoryService.stockOut(request);

        // Assert
        assertNotNull(result);
        assertEquals(30, result.getQuantity());
        assertEquals(10, result.getReservedQuantity());

        // Verify
        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void stockOut_shouldThrowExceptionWhenStockIsInsufficient() {

        // Arrange
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(50);

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setQuantity(30);
        inventory.setReservedQuantity(10);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(inventory));

        // Act & Assert
        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.stockOut(request)
        );

        // Verify
        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void stockOut_shouldThrowExceptionWhenInventoryNotFound() {

        // Arrange
        InventoryRequest request = new InventoryRequest();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(20);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.stockOut(request)
        );

        // Verify
        verify(inventoryRepository)
                .findByProductIdAndWarehouseId(1L, 1L);

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }


    @Test
    void getAllInventory_shouldReturnAllInventory() {

        // Arrange
        Inventory inventory1 = new Inventory();
        inventory1.setId(1L);
        inventory1.setQuantity(50);
        inventory1.setReservedQuantity(10);

        Inventory inventory2 = new Inventory();
        inventory2.setId(2L);
        inventory2.setQuantity(100);
        inventory2.setReservedQuantity(20);

        List<Inventory> inventories = List.of(
                inventory1,
                inventory2
        );

        when(inventoryRepository.findAll())
                .thenReturn(inventories);

        // Act
        List<Inventory> result = inventoryService.getAllInventory();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        // Verify
        verify(inventoryRepository).findAll();
    }
}
