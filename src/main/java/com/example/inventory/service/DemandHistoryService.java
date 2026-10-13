package com.example.inventory.service;

import com.example.inventory.dto.DemandHistoryResponse;
import com.example.inventory.entity.DemandHistory;
import com.example.inventory.repository.DemandHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DemandHistoryService {

    private final DemandHistoryRepository demandHistoryRepository;

    public DemandHistoryService(
            DemandHistoryRepository demandHistoryRepository) {
        this.demandHistoryRepository = demandHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<DemandHistoryResponse> getDemandHistory(
            Long productId,
            Long warehouseId,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date"
            );
        }

        List<DemandHistory> demandHistoryList =
                demandHistoryRepository
                        .findByProductIdAndWarehouseIdAndDemandDateBetweenOrderByDemandDateAsc(
                                productId,
                                warehouseId,
                                startDate,
                                endDate
                        );

        return demandHistoryList.stream()
                .map(history -> new DemandHistoryResponse(
                        history.getId(),
                        history.getProduct().getId(),
                        history.getWarehouse().getId(),
                        history.getDemandDate(),
                        history.getQuantity()
                ))
                .toList();
    }
}