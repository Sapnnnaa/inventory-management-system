package com.example.inventory.service;

import com.example.inventory.dto.ForecastAccuracyResponse;
import com.example.inventory.entity.DemandHistory;
import com.example.inventory.repository.DemandHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastAccuracyService {

    private static final int MOVING_AVERAGE_DAYS = 7;

    private final DemandHistoryRepository demandHistoryRepository;

    public ForecastAccuracyService(
            DemandHistoryRepository demandHistoryRepository) {
        this.demandHistoryRepository = demandHistoryRepository;
    }

    @Transactional(readOnly = true)
    public ForecastAccuracyResponse evaluateForecast(
            Long productId,
            Long warehouseId,
            LocalDate startDate,
            LocalDate endDate) {

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be positive"
            );
        }

        if (warehouseId == null || warehouseId <= 0) {
            throw new IllegalArgumentException(
                    "Warehouse ID must be positive"
            );
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date"
            );
        }

        LocalDate historyStartDate =
                startDate.minusDays(MOVING_AVERAGE_DAYS);

        List<DemandHistory> history =
                demandHistoryRepository
                        .findByProductIdAndWarehouseIdAndDemandDateBetweenOrderByDemandDateAsc(
                                productId,
                                warehouseId,
                                historyStartDate,
                                endDate
                        );

        Map<LocalDate, Integer> demandByDate = new HashMap<>();

        for (DemandHistory record : history) {
            demandByDate.put(
                    record.getDemandDate(),
                    record.getQuantity()
            );
        }

        List<ForecastAccuracyResponse.DailyAccuracy> dailyResults =
                new ArrayList<>();

        double totalAbsoluteError = 0.0;
        double totalPercentageError = 0.0;

        int evaluatedDays = 0;
        int percentageErrorDays = 0;
        int excludedDays = 0;

        LocalDate evaluationDate = startDate;

        while (!evaluationDate.isAfter(endDate)) {

            Integer actualQuantity =
                    demandByDate.get(evaluationDate);

            boolean hasCompleteHistory = true;
            int historicalTotal = 0;

            for (int i = MOVING_AVERAGE_DAYS; i >= 1; i--) {

                LocalDate previousDate =
                        evaluationDate.minusDays(i);

                Integer previousQuantity =
                        demandByDate.get(previousDate);

                if (previousQuantity == null) {
                    hasCompleteHistory = false;
                    break;
                }

                historicalTotal += previousQuantity;
            }

            if (actualQuantity == null || !hasCompleteHistory) {
                excludedDays++;
                evaluationDate = evaluationDate.plusDays(1);
                continue;
            }

            double averageDemand =
                    historicalTotal / (double) MOVING_AVERAGE_DAYS;

            int predictedQuantity =
                    (int) Math.round(averageDemand);

            int absoluteError =
                    Math.abs(actualQuantity - predictedQuantity);

            Double percentageError = null;

            if (actualQuantity > 0) {
                percentageError =
                        absoluteError * 100.0 / actualQuantity;

                totalPercentageError += percentageError;
                percentageErrorDays++;
            }

            totalAbsoluteError += absoluteError;
            evaluatedDays++;

            dailyResults.add(
                    new ForecastAccuracyResponse.DailyAccuracy(
                            evaluationDate,
                            actualQuantity,
                            predictedQuantity,
                            absoluteError,
                            percentageError
                    )
            );

            evaluationDate = evaluationDate.plusDays(1);
        }

        if (evaluatedDays == 0) {
            throw new IllegalArgumentException(
                    "No evaluable demand history found. "
                            + "Ensure the selected date range has actual demand "
                            + "and seven preceding days of history."
            );
        }

        double mae = totalAbsoluteError / evaluatedDays;

        Double mape = percentageErrorDays > 0
                ? totalPercentageError / percentageErrorDays
                : null;

        return new ForecastAccuracyResponse(
                productId,
                warehouseId,
                startDate,
                endDate,
                evaluatedDays,
                excludedDays,
                roundToTwoDecimals(mae),
                mape == null ? null : roundToTwoDecimals(mape),
                dailyResults
        );
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}