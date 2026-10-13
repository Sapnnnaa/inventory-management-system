package com.example.inventory.controller;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.example.inventory.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;


    // =========================================================
    // 1. CREATE PRODUCT - SUCCESS
    // =========================================================

    @Test
    void createProduct_shouldCreateProductSuccessfully() throws Exception {

        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 15");
        request.setSku("IP15-128");
        request.setDescription("Apple iPhone 15");
        request.setPrice(new BigDecimal("60000.00"));

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setDescription("Apple iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));

        when(productService.createProduct(any(ProductRequest.class)))
                .thenReturn(product);

        // Act & Assert
        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.sku").value("IP15-128"))
                .andExpect(jsonPath("$.description")
                        .value("Apple iPhone 15"))
                .andExpect(jsonPath("$.price").value(60000.00));

        verify(productService)
                .createProduct(any(ProductRequest.class));
    }


    // =========================================================
    // 2. GET ALL PRODUCTS
    // =========================================================

    @Test
    void getAllProducts_shouldReturnProductsSuccessfully()
            throws Exception {

        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone 15");
        product1.setSku("IP15-128");
        product1.setPrice(new BigDecimal("60000.00"));

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Samsung S24");
        product2.setSku("S24-256");
        product2.setPrice(new BigDecimal("70000.00"));

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product1, product2),
                        PageRequest.of(0, 10),
                        2
                );

        when(productService.getAllProducts(any()))
                .thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(
                        get("/api/products")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name")
                        .value("iPhone 15"))
                .andExpect(jsonPath("$.content[1].name")
                        .value("Samsung S24"));

        verify(productService)
                .getAllProducts(any());
    }


    // =========================================================
    // 3. GET PRODUCT BY ID - SUCCESS
    // =========================================================

    @Test
    void getProductById_shouldReturnProductSuccessfully()
            throws Exception {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setDescription("Apple iPhone 15");
        product.setPrice(new BigDecimal("60000.00"));

        when(productService.getProductById(1L))
                .thenReturn(product);

        // Act & Assert
        mockMvc.perform(
                        get("/api/products/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("iPhone 15"))
                .andExpect(jsonPath("$.sku")
                        .value("IP15-128"))
                .andExpect(jsonPath("$.price")
                        .value(60000.00));

        verify(productService)
                .getProductById(1L);
    }


    // =========================================================
    // 4. GET PRODUCT BY ID - NOT FOUND
    // =========================================================

    @Test
    void getProductById_shouldThrowExceptionWhenProductNotFound()
            throws Exception {

        // Arrange
        when(productService.getProductById(999L))
                .thenThrow(
                        new ProductNotFoundException(
                                "Product not found with id: 999"
                        )
                );

        // Act & Assert
        mockMvc.perform(
                        get("/api/products/999")
                )
                .andExpect(status().isNotFound());

        verify(productService)
                .getProductById(999L);
    }


    // =========================================================
    // 5. UPDATE PRODUCT - SUCCESS
    // =========================================================

    @Test
    void updateProduct_shouldUpdateProductSuccessfully()
            throws Exception {

        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 15 Pro");
        request.setSku("IP15-PRO");
        request.setDescription("Updated iPhone");
        request.setPrice(new BigDecimal("80000.00"));

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15 Pro");
        product.setSku("IP15-PRO");
        product.setDescription("Updated iPhone");
        product.setPrice(new BigDecimal("80000.00"));

        when(productService.updateProduct(
                eq(1L),
                any(ProductRequest.class)
        )).thenReturn(product);

        // Act & Assert
        mockMvc.perform(
                        put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.sku")
                        .value("IP15-PRO"))
                .andExpect(jsonPath("$.price")
                        .value(80000.00));

        verify(productService)
                .updateProduct(
                        eq(1L),
                        any(ProductRequest.class)
                );
    }


    // =========================================================
    // 6. UPDATE PRODUCT - NOT FOUND
    // =========================================================

    @Test
    void updateProduct_shouldThrowExceptionWhenProductNotFound()
            throws Exception {

        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 15 Pro");
        request.setSku("IP15-PRO");
        request.setDescription("Updated iPhone");
        request.setPrice(new BigDecimal("80000.00"));

        when(productService.updateProduct(
                eq(999L),
                any(ProductRequest.class)
        )).thenThrow(
                new ProductNotFoundException(
                        "Product not found with id: 999"
                )
        );

        // Act & Assert
        mockMvc.perform(
                        put("/api/products/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound());

        verify(productService)
                .updateProduct(
                        eq(999L),
                        any(ProductRequest.class)
                );
    }


    // =========================================================
    // 7. DELETE PRODUCT - SUCCESS
    // =========================================================

    @Test
    void deleteProduct_shouldDeleteProductSuccessfully()
            throws Exception {

        // Arrange
        doNothing()
                .when(productService)
                .deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(
                        delete("/api/products/1")
                )
                .andExpect(status().isNoContent());

        verify(productService)
                .deleteProduct(1L);
    }


    // =========================================================
    // 8. DELETE PRODUCT - NOT FOUND
    // =========================================================

    @Test
    void deleteProduct_shouldThrowExceptionWhenProductNotFound()
            throws Exception {

        // Arrange
        doThrow(
                new ProductNotFoundException(
                        "Product not found with id: 999"
                )
        )
                .when(productService)
                .deleteProduct(999L);

        // Act & Assert
        mockMvc.perform(
                        delete("/api/products/999")
                )
                .andExpect(status().isNotFound());

        verify(productService)
                .deleteProduct(999L);
    }


    // =========================================================
    // 9. SEARCH PRODUCTS
    // =========================================================

    @Test
    void searchProducts_shouldReturnMatchingProducts()
            throws Exception {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setPrice(new BigDecimal("60000.00"));

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(0, 10),
                        1
                );

        when(productService.searchProducts(
                eq("iPhone"),
                eq(new BigDecimal("50000")),
                eq(new BigDecimal("70000")),
                any()
        )).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(
                        get("/api/products/search")
                                .param("name", "iPhone")
                                .param("minPrice", "50000")
                                .param("maxPrice", "70000")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("iPhone 15"));

        verify(productService)
                .searchProducts(
                        eq("iPhone"),
                        eq(new BigDecimal("50000")),
                        eq(new BigDecimal("70000")),
                        any()
                );
    }


    // =========================================================
    // 10. SEARCH WITHOUT OPTIONAL PARAMETERS
    // =========================================================

    @Test
    void searchProducts_shouldWorkWithoutOptionalParameters()
            throws Exception {

        // Arrange
        Page<Product> productPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        when(productService.searchProducts(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(
                        get("/api/products/search")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(0));

        verify(productService)
                .searchProducts(
                        isNull(),
                        isNull(),
                        isNull(),
                        any()
                );
    }


    // =========================================================
    // 11. CREATE PRODUCT - VALIDATION FAILURE
    // =========================================================

    @Test
    void createProduct_shouldReturnBadRequestWhenRequestIsInvalid()
            throws Exception {

        // Arrange
        ProductRequest request = new ProductRequest();

        request.setName("");
        request.setSku("");
        request.setDescription("");
        request.setPrice(null);

        // Act & Assert
        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        // Service should NOT be called
        verify(productService, never())
                .createProduct(any(ProductRequest.class));
    }


    // =========================================================
    // 12. UPDATE PRODUCT - VALIDATION FAILURE
    // =========================================================

    @Test
    void updateProduct_shouldReturnBadRequestWhenRequestIsInvalid()
            throws Exception {

        // Arrange
        ProductRequest request = new ProductRequest();

        request.setName("");
        request.setSku("");
        request.setDescription("");
        request.setPrice(null);

        // Act & Assert
        mockMvc.perform(
                        put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        // Service should NOT be called
        verify(productService, never())
                .updateProduct(
                        anyLong(),
                        any(ProductRequest.class)
                );
    }


    // =========================================================
    // 13. PAGINATION AND SORTING
    // =========================================================

    @Test
    void getAllProducts_shouldSupportPaginationAndSorting()
            throws Exception {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setPrice(new BigDecimal("60000.00"));

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(1, 5),
                        6
                );

        when(productService.getAllProducts(any()))
                .thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(
                        get("/api/products")
                                .param("page", "1")
                                .param("size", "5")
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(1));

        verify(productService)
                .getAllProducts(any());
    }


    // =========================================================
    // 14. SEARCH WITH ONLY NAME
    // =========================================================

    @Test
    void searchProducts_shouldSearchByNameOnly()
            throws Exception {

        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setPrice(new BigDecimal("60000.00"));

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(0, 10),
                        1
                );

        when(productService.searchProducts(
                eq("iPhone"),
                isNull(),
                isNull(),
                any()
        )).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(
                        get("/api/products/search")
                                .param("name", "iPhone")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("iPhone 15"));

        verify(productService)
                .searchProducts(
                        eq("iPhone"),
                        isNull(),
                        isNull(),
                        any()
                );
    }
}