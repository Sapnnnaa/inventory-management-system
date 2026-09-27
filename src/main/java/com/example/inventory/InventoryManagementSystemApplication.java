package com.example.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class InventoryManagementSystemApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				InventoryManagementSystemApplication.class, args);
	}

}
