package com.example.inventory.config;

import com.example.inventory.entity.DemandHistory;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Warehouse;
import com.example.inventory.repository.DemandHistoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.WarehouseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
public class DemandHistoryDataSeeder implements CommandLineRunner {

    private final DemandHistoryRepository demandHistoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    private final Random random = new Random();

    public DemandHistoryDataSeeder(
            DemandHistoryRepository demandHistoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository) {

        this.demandHistoryRepository = demandHistoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public void run(String... args) {

        // Prevent duplicate data every time application starts
        if (demandHistoryRepository.count() > 0) {
            return;
        }

        List<Product> products = productRepository.findAll();
        List<Warehouse> warehouses = warehouseRepository.findAll();

        if (products.isEmpty() || warehouses.isEmpty()) {
            return;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(89);

        for (Product product : products) {

            for (Warehouse warehouse : warehouses) {

                generateDemand(
                        product,
                        warehouse,
                        startDate,
                        endDate
                );
            }
        }

        System.out.println(
                "Demand history test data generated successfully."
        );
    }

    private void generateDemand(
            Product product,
            Warehouse warehouse,
            LocalDate startDate,
            LocalDate endDate) {

        int baseDemand = calculateBaseDemand(product);

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {

            int quantity = generateDailyDemand(
                    baseDemand,
                    currentDate
            );

            DemandHistory demandHistory =
                    new DemandHistory();

            demandHistory.setProduct(product);
            demandHistory.setWarehouse(warehouse);
            demandHistory.setDemandDate(currentDate);
            demandHistory.setQuantity(quantity);

            demandHistoryRepository.save(
                    demandHistory
            );

            currentDate = currentDate.plusDays(1);
        }
    }

    private int calculateBaseDemand(Product product) {

        if (product.getPrice() == null) {
            return 10;
        }

        double price =
                product.getPrice().doubleValue();

        if (price < 1000) {
            return 30;
        }

        if (price < 10000) {
            return 20;
        }

        if (price < 50000) {
            return 12;
        }

        return 8;
    }

    private int generateDailyDemand(
            int baseDemand,
            LocalDate date) {

        int demand = baseDemand;

        DayOfWeek dayOfWeek =
                date.getDayOfWeek();

        // Weekend effect
        if (dayOfWeek == DayOfWeek.SATURDAY
                || dayOfWeek == DayOfWeek.SUNDAY) {

            demand -= 2;
        }

        // Random daily variation
        demand += random.nextInt(7) - 3;

        // Occasional demand spike
        if (random.nextDouble() < 0.05) {

            demand +=
                    Math.max(5, baseDemand / 2);
        }

        // Demand should never be negative
        return Math.max(demand, 1);
    }
}