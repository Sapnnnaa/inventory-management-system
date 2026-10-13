package com.example.inventory.dto;

import java.time.LocalDate;
import java.util.List;

public record DemandForecastResponse(
        Long productId,
        Long warehouseId,
        LocalDate forecastStartDate,
        LocalDate forecastEndDate,
        int historicalDaysUsed,
        double averageDailyDemand,
        int totalForecastDemand,
        List<DailyForecast> forecasts
) {

    public record DailyForecast(
            LocalDate date,
            int predictedQuantity
    ) {
    }
}