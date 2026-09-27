package com.example.inventory.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Inventory & Order Management System API")
                        .description(
                                "REST APIs for managing products, warehouses, "+
                                "inventory, stock, and customer orders."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sapna")
                                .email("sapna9082000@gmail.com")
                        )
                );
    }
}
