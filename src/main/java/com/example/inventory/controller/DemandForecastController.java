package com.example.inventory.controller;

import com.example.inventory.dto.DemandForecastResponse;
import com.example.inventory.dto.ForecastAccuracyResponse;
import com.example.inventory.service.DemandForecastService;
import com.example.inventory.service.ForecastAccuracyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/forecasts")
public class DemandForecastController {

    private final DemandForecastService demandForecastService;
    private final ForecastAccuracyService forecastAccuracyService;

    public DemandForecastController(
            DemandForecastService demandForecastService,
            ForecastAccuracyService forecastAccuracyService) {

        this.demandForecastService = demandForecastService;
        this.forecastAccuracyService = forecastAccuracyService;
    }

    @GetMapping("/demand/{productId}/{warehouseId}")
    public DemandForecastResponse forecastDemand(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "7") int days) {

        return demandForecastService.forecastDemand(
                productId,
                warehouseId,
                days
        );
    }

    @GetMapping("/accuracy/{productId}/{warehouseId}")
    public ForecastAccuracyResponse evaluateForecast(
            @PathVariable Long productId,
            @PathVariable Long warehouseId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return forecastAccuracyService.evaluateForecast(
                productId,
                warehouseId,
                startDate,
                endDate
        );
    }
}