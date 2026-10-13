
package com.example.inventory.ai;

import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.SmartReplenishmentService;
import com.example.inventory.dto.ReplenishmentResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class InventoryAiTools {

    private final ProductRepository productRepository;
    private final SmartReplenishmentService smartReplenishmentService;
    private final InventoryRepository inventoryRepository;

    public InventoryAiTools(
            ProductRepository productRepository,
            SmartReplenishmentService smartReplenishmentService,
            InventoryRepository inventoryRepository) {

        this.productRepository = productRepository;
        this.smartReplenishmentService = smartReplenishmentService;
        this.inventoryRepository = inventoryRepository;
    }

    @Tool(description = """
            Find a product by its numeric product ID.
            Use this tool when the user asks about a product's name,
            SKU, description, or price.
            """)
    public String getProductDetails(Long productId) {

        if (productId == null || productId <= 0) {
            return "Invalid product ID. It must be a positive number.";
        }

        return productRepository.findById(productId)
                .map(this::formatProduct)
                .orElse("No product found with ID " + productId);
    }

    @Tool(description = """
            Get a smart stock replenishment recommendation for a product
            at a warehouse. Requires product ID, warehouse ID, lead time
            in days, and safety stock units. Use this for questions about
            whether to reorder and how many units are suggested.
            This tool only reads data and does not place an order.
            """)
    public String getReplenishmentRecommendation(
            Long productId,
            Long warehouseId,
            int leadTimeDays,
            int safetyStock) {

        ReplenishmentResponse result =
                smartReplenishmentService.recommendReplenishment(
                        productId,
                        warehouseId,
                        leadTimeDays,
                        safetyStock
                );

        return """
                Product ID: %d
                Warehouse ID: %d
                Current stock: %d
                Reserved stock: %d
                Available stock: %d
                Lead time days: %d
                Average daily demand: %.2f
                Forecast demand during lead time: %d
                Safety stock: %d
                Reorder point: %d
                Suggested reorder quantity: %d
                Recommended order date: %s
                """.formatted(
                result.productId(),
                result.warehouseId(),
                result.currentStock(),
                result.reservedStock(),
                result.availableStock(),
                result.leadTimeDays(),
                result.averageDailyDemand(),
                result.leadTimeDemand(),
                result.safetyStock(),
                result.reorderPoint(),
                result.suggestedReorderQuantity(),
                result.recommendedOrderDate() == null
                        ? "No order recommended right now"
                        : result.recommendedOrderDate()
        );
    }


    @Tool(description = """
        Find products and warehouses that need replenishment.
        Use this when asked which products are at risk of running out
        of stock. Evaluates all inventory records using the existing
        Java replenishment service, with a 7-day lead time and 10 units
        of safety stock. This tool only reads data.
        """)
    public String getAtRiskInventory() {

        List<Inventory> inventories = inventoryRepository.findAll();

        if (inventories.isEmpty()) {
            return "No inventory records were found.";
        }

        StringBuilder report = new StringBuilder();
        int atRiskCount = 0;
        int failedCount = 0;

        for (Inventory inventory : inventories) {

            Long productId = inventory.getProduct().getId();
            Long warehouseId = inventory.getWarehouse().getId();

            try {
                ReplenishmentResponse result =
                        smartReplenishmentService.recommendReplenishment(
                                productId,
                                warehouseId,
                                7,
                                10
                        );

                if (result.suggestedReorderQuantity() > 0) {
                    atRiskCount++;

                    report.append("\nProduct: ")
                            .append(inventory.getProduct().getName())
                            .append("\nSKU: ")
                            .append(inventory.getProduct().getSku())
                            .append("\nProduct ID: ")
                            .append(productId)
                            .append("\nWarehouse ID: ")
                            .append(warehouseId)
                            .append("\nAvailable stock: ")
                            .append(result.availableStock())
                            .append("\nReorder point: ")
                            .append(result.reorderPoint())
                            .append("\nSuggested reorder quantity: ")
                            .append(result.suggestedReorderQuantity())
                            .append(" units\n");
                }

            } catch (RuntimeException exception) {
                failedCount++;
            }
        }

        if (atRiskCount == 0 && failedCount == 0) {
            return "No inventory records currently require replenishment "
                    + "under the configured 7-day lead time and "
                    + "10-unit safety stock assumptions.";
        }

        StringBuilder response = new StringBuilder(
                "Inventory risk report\n"
                        + "Lead time: 7 days\n"
                        + "Safety stock: 10 units\n"
                        + "Products requiring replenishment: "
                        + atRiskCount + "\n"
        );

        response.append(report);

        if (failedCount > 0) {
            response.append("\nWarning: ")
                    .append(failedCount)
                    .append(" inventory records could not be evaluated. ")
                    .append("The report may be incomplete.");
        }

        return response.toString();
    }


    private String formatProduct(Product product) {
        return """
                Product ID: %s
                Name: %s
                SKU: %s
                Description: %s
                Price: %s
                """.formatted(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice()
        );
    }
}
