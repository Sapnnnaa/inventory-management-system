package com.example.inventory.repository;

import com.example.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByProductIdAndWarehouseId(
            Long productId,
            Long warehouseId
    );

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.product.id = :productId
              AND i.warehouse.id = :warehouseId
            """)
    Optional<Inventory> findInventoryForReplenishment(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );
}