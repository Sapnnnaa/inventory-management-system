package com.example.inventory.dto;

import java.time.LocalDate;
import java.util.List;

public record ForecastAccuracyResponse(
        Long productId,
        Long warehouseId,
        LocalDate evaluationStartDate,
        LocalDate evaluationEndDate,
        int evaluationDays,
        int excludedDays,
        double meanAbsoluteError,
        Double meanAbsolutePercentageError,
        List<DailyAccuracy> dailyResults
) {

    public record DailyAccuracy(
            LocalDate date,
            int actualQuantity,
            int predictedQuantity,
            int absoluteError,
            Double percentageError
    ) {
    }
}