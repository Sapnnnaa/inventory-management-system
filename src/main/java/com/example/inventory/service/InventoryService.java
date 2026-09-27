package com.example.inventory.service;


import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Warehouse;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.InventoryNotFoundException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.exception.WarehouseNotFoundException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository) {

        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public Inventory createInventory(InventoryRequest request){

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found with id: "+ request.getProductId())
                );

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() ->
                        new WarehouseNotFoundException("Warehouse not found with id: "+ request.getWarehouseId())
                );

        Inventory inventory =  new Inventory();

        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(request.getQuantity());
        inventory.setReservedQuantity(0);

        return inventoryRepository.save(inventory);

    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryById(Long id){

        return inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new InventoryNotFoundException(" Inventory not found with id: "+ id)
                );
    }

    @Transactional
    public Inventory stockIn(InventoryRequest request) {

        Optional<Inventory> existingInventory =
                inventoryRepository.findByProductIdAndWarehouseId(
                        request.getProductId(),
                        request.getWarehouseId()
                );

        if (existingInventory.isPresent()) {

            Inventory inventory = existingInventory.get();

            inventory.setQuantity(
                    inventory.getQuantity() + request.getQuantity()
            );

            return inventoryRepository.save(inventory);
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: "
                                        + request.getProductId()
                        )
                );

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse not found with id: "
                                        + request.getWarehouseId()
                        )
                );

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(request.getQuantity());

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory stockOut(InventoryRequest request){

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(
                        request.getProductId(),
                        request.getWarehouseId()
                )
                .orElseThrow(()->
                        new InventoryNotFoundException(
                                "Inventory not found for product id: "
                                        + request.getProductId()
                                        + "and warehouse id: "
                                        + request.getWarehouseId()
                        )
                );

        int availableQuantity = inventory.getQuantity()- inventory.getReservedQuantity();

        if (availableQuantity < request.getQuantity()){

            throw new InsufficientStockException(
                    "Insufficient stock. Available stock: "
                            + inventory.getQuantity()
                            + ", requested: "
                            + request.getQuantity()
            );
        }
        inventory.setQuantity(
                inventory.getQuantity() - request.getQuantity()
        );

        return inventoryRepository.save(inventory);
    }

}
