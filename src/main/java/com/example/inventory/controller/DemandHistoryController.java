package com.example.inventory.controller;

import com.example.inventory.dto.DemandHistoryResponse;
import com.example.inventory.service.DemandHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/demand-history")
public class DemandHistoryController {

    private final DemandHistoryService demandHistoryService;

    public DemandHistoryController(
            DemandHistoryService demandHistoryService) {
        this.demandHistoryService = demandHistoryService;
    }

    @GetMapping("/{productId}/{warehouseId}")
    public List<DemandHistoryResponse> getDemandHistory(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return demandHistoryService.getDemandHistory(
                productId,
                warehouseId,
                startDate,
                endDate
        );
    }
}