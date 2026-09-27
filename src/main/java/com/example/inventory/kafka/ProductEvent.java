package com.example.inventory.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    private Long productId;
    private String name;
    private String sku;
    private BigDecimal price;
}
