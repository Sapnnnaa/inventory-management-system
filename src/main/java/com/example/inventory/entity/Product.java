package com.example.inventory.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Schema(
            description = "Unique ID of the product",
            example = "1"
    )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Schema(
            description = "Name of the product",
            example = "iPhone 15"
    )
    @Column(nullable = false)
    private String name;


    @Schema(
            description = "Unique SKU of the product",
            example = "IPH15-128GB"
    )
    @Column(nullable = false, unique = true)
    private String sku;


    @Schema(
            description = "Description of the product",
            example = "Apple iPhone 15 with 128GB storage"
    )
    private String description;


    @Schema(
            description = "Price of the product",
            example = "69999.00"
    )
    @Column(nullable = false)
    private BigDecimal price;
}