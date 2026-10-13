package com.example.inventory.service;

import com.example.inventory.dto.DemandForecastResponse;
import com.example.inventory.entity.DemandHistory;
import com.example.inventory.repository.DemandHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemandForecastService {

    private static final int MOVING_AVERAGE_DAYS = 7;

    private final DemandHistoryRepository demandHistoryRepository;

    public DemandForecastService(
            DemandHistoryRepository demandHistoryRepository) {
        this.demandHistoryRepository = demandHistoryRepository;
    }

    @Transactional(readOnly = true)
    public DemandForecastResponse forecastDemand(
            Long productId,
            Long warehouseId,
            int forecastDays) {

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

        if (forecastDays < 1 || forecastDays > 90) {
            throw new IllegalArgumentException(
                    "Forecast days must be between 1 and 90"
            );
        }

        LocalDate today = LocalDate.now();

        LocalDate historicalStartDate =
                today.minusDays(MOVING_AVERAGE_DAYS);

        LocalDate historicalEndDate = today.minusDays(1);

        List<DemandHistory> history =
                demandHistoryRepository
                        .findByProductIdAndWarehouseIdAndDemandDateBetweenOrderByDemandDateAsc(
                                productId,
                                warehouseId,
                                historicalStartDate,
                                historicalEndDate
                        );

        if (history.isEmpty()) {
            throw new IllegalArgumentException(
                    "No historical demand found for the given product and warehouse"
            );
        }

        int totalHistoricalDemand = history.stream()
                .mapToInt(DemandHistory::getQuantity)
                .sum();

        double averageDailyDemand =
                totalHistoricalDemand / (double) history.size();

        int predictedDailyDemand =
                (int) Math.round(averageDailyDemand);

        LocalDate forecastStartDate = today;
        LocalDate forecastEndDate =
                today.plusDays(forecastDays - 1L);

        List<DemandForecastResponse.DailyForecast> forecasts =
                new ArrayList<>();

        for (int day = 0; day < forecastDays; day++) {

            LocalDate forecastDate =
                    forecastStartDate.plusDays(day);

            forecasts.add(
                    new DemandForecastResponse.DailyForecast(
                            forecastDate,
                            predictedDailyDemand
                    )
            );
        }

        int totalForecastDemand =
                predictedDailyDemand * forecastDays;

        return new DemandForecastResponse(
                productId,
                warehouseId,
                forecastStartDate,
                forecastEndDate,
                history.size(),
                Math.round(averageDailyDemand * 100.0) / 100.0,
                totalForecastDemand,
                forecasts
        );
    }
}