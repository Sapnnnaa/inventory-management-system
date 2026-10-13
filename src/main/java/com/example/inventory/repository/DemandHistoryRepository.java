package com.example.inventory.repository;

import com.example.inventory.entity.DemandHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DemandHistoryRepository
        extends JpaRepository<DemandHistory, Long> {

    List<DemandHistory> findByProductIdAndWarehouseIdAndDemandDateBetweenOrderByDemandDateAsc(
            Long productId,
            Long warehouseId,
            LocalDate startDate,
            LocalDate endDate
    );
}