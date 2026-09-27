package com.example.inventory.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class Warehouse {

    @Schema(
            description = "Unique ID of the warehouse",
            example = "1"
    )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Schema(
            description = "Name of the warehouse",
            example = "Main Warehouse"
    )
    @Column(nullable = false)
    private String name;


    @Schema(
            description = "Unique warehouse code",
            example = "WH-001"
    )
    @Column(nullable = false, unique = true)
    private String code;


    @Schema(
            description = "Warehouse location",
            example = "Dhanbad"
    )
    @Column(nullable = false)
    private String location;
}
