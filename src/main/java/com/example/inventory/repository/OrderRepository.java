package com.example.inventory.repository;

import com.example.inventory.entity.Order;
import com.example.inventory.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.items oi
            WHERE oi.product.id = :productId
              AND o.warehouse.id = :warehouseId
              AND o.status IN :statuses
              AND o.orderDate BETWEEN :startDate AND :endDate
            ORDER BY o.orderDate ASC
            """)
    List<Order> findOrdersForDemandAnalysis(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}